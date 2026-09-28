# 데이터베이스 테이블 설계 및 프로젝트 아키텍처

## 1. 아키텍처 원칙 & 코딩 룰 (Core Architecture Rules)

1. **User Natural Key (Email) Rule:**
   - `users` 테이블의 Primary Key는 `email (VARCHAR(100))`을 사용합니다.
   - `business_info`, `reviews`, `favorites`, `chat_rooms` 등 연관 테이블의 FK는 `user_id`가 아닌 `email`을 직접 참조합니다. (`ON UPDATE CASCADE ON DELETE CASCADE`)

2. **Pre-computed Read Cache & Exception Rules (AI 호출 정책):**
   - **[목록 조회 최적화]:** 메인 및 공고 목록 조회(`GET /api/programs`) 시 실시간 LLM을 일괄 호출하는 것은 절대 금지하며, DB에 pre-compute되어 있는 `reviews` 및 `program_documents`를 JOIN 조회하여 초고속 서빙합니다.
   - **[온디맨드 검수]:** 유저의 특정 공고 검수 데이터(`reviews`)가 없으면 백엔드가 비동기/온디맨드로 AI 검수를 수행 후 서빙합니다.
   - **[공고 변경 감지 및 재검수]:** 외부 API의 `updtPnttm` 변경 또는 `reviews.is_outdated == TRUE` 시, 최신 공고 기반으로 AI 요약(`program_documents`) 및 재검수(`reviews`)를 수행하고 `is_outdated = FALSE`로 복원합니다.

3. **Snapshot Isolation for Reviews:**
   - `reviews` 테이블 생성 시 검수 당시의 유저 사업자 프로필을 `business_snapshot (JSON)`으로 함께 저장하여, 마이페이지 프로필 수정 시에도 과거 검수 이력의 무결성을 보장합니다.

4. **Redis Separation Rule:**
   - SMS 인증번호, JWT Refresh Token, 임시 검증 토큰은 RDBMS가 아닌 **Redis**에서 휘발성 인메모리 데이터로 처리합니다.

5. **UI Status Mapping Rule:**
   - `reviews.status` 컬럼은 다음 3가지 상태값과 UI 뱃지 색상을 1:1 매핑합니다:
     - `'MATCHED'`: 신청 가능 (🟢 초록색)
     - `'UNMATCHED'`: 신청 불가 (🔴 빨간색)
     - `'NEED_CHECK'`: 조건 확인 필요 (🟡 노란색)

---

## 2. RDBMS 테이블 역할 및 명세

| 테이블명 | 역할 및 주요 특징 |
| --- | --- |
| `users` | **사용자 계정**: `email`을 PK로 사용하여 시스템 전체 식별자로 활용. |
| `business_info` | **사업자 프로필 (1:N)**: 업종, 지역, 개업일(`opening_date`), 사업자 유형(`business_type`), 직원 수, 매출액 보관. `is_default = true`인 대표 사업자를 우선으로 AI 검수에 활용. |
| `business_interest_categories` | **사업자 관심 카테고리 (1:N)**: 사업자별 다중 관심 분야 저장. `(business_id, category)` UNIQUE 제약조건 적용. |
| `programs` | **지원사업 공고 마스터**: 외부 API 공고의 기본 정보(제목, 기관, 접수기간)와 원본 타임스탬프(`api_updated_at`) 저장. |
| `program_documents` | **공고 원문 및 AI 요약 (1:1)**: 원문(`original_text`)과 LLM 요약문(`ai_summary`)을 보관하여 챗봇 Context 제공 및 조회 속도 최적화. |
| `reviews` | **AI 사전 검수 결과**: 검수 상태(`MATCHED`, `UNMATCHED`, `NEED_CHECK`) 및 세부 사유(`result_detail (JSON)`), 사업자 스냅샷(`business_snapshot (JSON)`) 보관. 공고 수정 시 `is_outdated` 플래그 활용. |
| `favorites` | **관심 공고**: 유저의 즐겨찾기 공고 관리 (Hard Delete). `(email, pblanc_id)` UNIQUE 제약조건으로 중복 방지. |
| `chat_rooms` | **AI 챗봇 세션**: 질문 대상 공고(`pblanc_id`) 연동. 공고 삭제 시 대화 보존을 위해 `ON DELETE SET NULL` 적용. |
| `chat_messages` | **챗봇 대화 메시지**: 발신자 구분(`sender_type`: `USER` / `BOT`)을 ENUM으로 제한하여 보관. |

---

## 3. 주요 테이블 관계

- `users` 1 : N `business_info`
- `business_info` 1 : N `business_interest_categories`
- `programs` 1 : 1 `program_documents`
- `users` 1 : N `reviews`
- `programs` 1 : N `reviews`
- `users` 1 : N `favorites`
- `programs` 1 : N `favorites`
- `users` 1 : N `chat_rooms`
- `programs` 1 : N `chat_rooms` (공고 삭제 시 `NULL` 세팅)
- `chat_rooms` 1 : N `chat_messages`

---

## 4. 주요 컬럼 상세

### business_info (사업자 정보)

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| `business_id` | BIGINT (PK) | 사업자 정보 식별자 |
| `email` | VARCHAR(100) (FK) | 사용자 이메일 (`users` 참조) |
| `company_name` | VARCHAR(100) | 상호명 |
| `industry` | VARCHAR(50) | 업종 |
| `region` | VARCHAR(50) | 지역 |
| `opening_date` | DATE | 개업일 |
| `business_type` | ENUM | `개인사업자` 또는 `법인사업자` |
| `employee_count` | INT | 직원 수 |
| `annual_revenue` | BIGINT | 연 매출 |
| `is_default` | BOOLEAN | 대표 사업자 여부 (기본값: TRUE) |

### reviews (AI 사전 검수 이력)

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| `review_id` | BIGINT (PK) | 검수 결과 식별자 |
| `email` | VARCHAR(100) (FK) | 사용자 이메일 (`users` 참조) |
| `pblanc_id` | VARCHAR(100) (FK) | 지원사업 공고 식별자 (`programs` 참조) |
| `business_snapshot` | JSON | 검수 시점의 사업자 프로필 스냅샷 |
| `status` | VARCHAR(20) | 검수 상태 (`MATCHED`, `UNMATCHED`, `NEED_CHECK`) |
| `result_detail` | JSON | AI 세부 검수 결과 사유 |
| `is_outdated` | BOOLEAN | 공고 변경 시 재검수 대상 여부 플래그 |

---

## 5. Redis 관리 데이터

| 항목 | 저장 위치 | 목적 및 특성 |
| --- | --- | --- |
| **SMS 인증번호** | Redis | 휘발성 인증 데이터 관리 (`phone_verifications`) 및 TTL 설정 |
| **JWT Refresh Token** | Redis | 사용자 세션 관리 및 보안 강화를 위한 휘발성 저장 |
| **임시 검증 토큰** | Redis | 회원가입 및 본인인증 단계 간 세션 보장용 |

## 6. 실제 테이블

-- =====================================================
-- 1. 사용자 테이블 (email이 PK)
-- =====================================================

CREATE TABLE users (
    email VARCHAR(100) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- =====================================================
-- 2. 사업자 정보 테이블
-- email 참조, 1:N 다중 사업자 지원
-- =====================================================

CREATE TABLE business_info (
    business_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    email VARCHAR(100) NOT NULL,
    company_name VARCHAR(100),

    industry VARCHAR(50) NOT NULL,
    region VARCHAR(50) NOT NULL,
    opening_date DATE NOT NULL,

    -- 사업자 유형 (개인사업자 / 법인사업자)
    business_type ENUM('개인사업자', '법인사업자') NOT NULL,

    employee_count INT NOT NULL,
    annual_revenue BIGINT NOT NULL,

    is_default BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_business_info_user
        FOREIGN KEY (email)
        REFERENCES users(email)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- =====================================================
-- 3. 사업자 관심 카테고리 테이블
-- 하나의 사업자에 여러 관심 카테고리 저장
-- =====================================================

CREATE TABLE business_interest_categories (
    interest_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    business_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_business_interest_business
        FOREIGN KEY (business_id)
        REFERENCES business_info(business_id)
        ON DELETE CASCADE,

    -- 동일 사업자의 동일 카테고리 중복 방지
    CONSTRAINT uk_business_category
        UNIQUE (business_id, category)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- =====================================================
-- 4. 지원사업 공고 테이블
-- updtPnttm 원본 저장용
-- =====================================================

CREATE TABLE programs (
    pblanc_id VARCHAR(100) PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    organization VARCHAR(100),

    target_description TEXT, 
    description TEXT,        

    apply_start_date DATE,
    apply_end_date DATE,
    raw_apply_period VARCHAR(30), 
    api_updated_at VARCHAR(30),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. 공고문 원문 및 AI 요약
-- 1:1 관계
-- =====================================================

CREATE TABLE program_documents (
    pblanc_id VARCHAR(100) PRIMARY KEY,

    original_text LONGTEXT,
    ai_summary TEXT,

    doc_hash VARCHAR(64),

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_program_documents_program
        FOREIGN KEY (pblanc_id)
        REFERENCES programs(pblanc_id)
        ON DELETE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- =====================================================
-- 6. AI 신청 전 검수 및 이력
-- 검수 상태: MATCHED / UNMATCHED / NEED_CHECK
-- =====================================================

CREATE TABLE reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    email VARCHAR(100) NOT NULL,
    pblanc_id VARCHAR(100) NOT NULL,

    -- 검수 당시 사업자 정보 스냅샷
    business_snapshot JSON NOT NULL,

    -- AI 검수 상태
    status VARCHAR(20) NOT NULL,

    -- AI 세부 검수 결과 및 사유
    result_detail JSON NOT NULL,

    -- 공고 변경 시 재검수 대상 여부
    is_outdated BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (email)
        REFERENCES users(email)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_reviews_program
        FOREIGN KEY (pblanc_id)
        REFERENCES programs(pblanc_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_reviews_status
        CHECK (status IN ('MATCHED', 'UNMATCHED', 'NEED_CHECK'))
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- =====================================================
-- 7. 관심 공고
-- email 참조, Hard Delete 구조
-- =====================================================

CREATE TABLE favorites (
    favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    email VARCHAR(100) NOT NULL,
    pblanc_id VARCHAR(100) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_email_program
        UNIQUE (email, pblanc_id),

    CONSTRAINT fk_favorites_user
        FOREIGN KEY (email)
        REFERENCES users(email)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_favorites_program
        FOREIGN KEY (pblanc_id)
        REFERENCES programs(pblanc_id)
        ON DELETE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- =====================================================
-- 8. 챗봇 대화 세션
-- 질문 대상 공고 FK 포함
-- =====================================================

CREATE TABLE chat_rooms (
    room_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    email VARCHAR(100) NOT NULL,
    pblanc_id VARCHAR(100) NULL,

    title VARCHAR(100) DEFAULT '지원사업 문의',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chat_rooms_user
        FOREIGN KEY (email)
        REFERENCES users(email)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_chat_rooms_program
        FOREIGN KEY (pblanc_id)
        REFERENCES programs(pblanc_id)
        ON DELETE SET NULL
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- =====================================================
-- 9. 챗봇 대화 메시지 이력
-- sender_type: USER / BOT
-- =====================================================

CREATE TABLE chat_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    room_id BIGINT NOT NULL,

    sender_type ENUM('USER', 'BOT') NOT NULL,

    content TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chat_messages_room
        FOREIGN KEY (room_id)
        REFERENCES chat_rooms(room_id)
        ON DELETE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;