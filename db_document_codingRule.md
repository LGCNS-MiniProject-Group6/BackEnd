#  Project Context & DB Schema Architecture

## 1. Project Overview & Database Architecture Goal

###  Database Purpose & Operation Flow (DB 작동 방향 및 목적)
본 데이터베이스는 **"AI 기반 맞춤형 정부 지원사업 추천 및 신청 자격 사전 검수 서비스"**를 위해 설계되었습니다. 5일간의 짧은 개발 기간과 타이트한 AI 토큰 비용을 극복하기 위해 **사전 처리(Pre-computation) & 캐싱 서빙** 아키텍처를 지향합니다.

1. **공고 수집 및 AI 요약 (Background Pipeline):**
   - 스케줄러가 외부 공고 API를 주기적으로 수집하고, 공고문(PDF/Text)을 LLM(AI)으로 사전 요약하여 `program_documents`에 저장합니다.
2. **초고속 캐싱 서빙 (0.01초 목록 조회):**
   - 사용자가 메인 페이지나 공고 목록을 조회할 때 실시간 AI를 호출하지 않고, 이미 검수·요약된 DB 데이터(`reviews`, `program_documents`)를 JOIN 조회하여 초고속으로 서빙합니다.
3. **온디맨드/자동 재검수 파이프라인 (On-Demand & Scheduled Review):**
   - 유저의 검수 이력이 없으면 그 시점에 AI 검수를 즉시 수행하여 `reviews`에 저장합니다.
   - 외부 API 공고 수정(`updtPnttm`) 감지 시, 백엔드 스케줄러가 공고 재요약 및 기존 유저 대상 재검수를 자동 실행하여 최신성을 유지합니다.

---

## 2. Table Roles & Responsibilities (각 테이블별 역할)

| 테이블명                | 핵심 역할 및 기능                                                                                                                                                                                                                                                          |
| :---------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`users`**             | **사용자 기본 계정.** `email`을 Primary Key(자연키)로 사용하여 시스템 전체의 고유 식별자로 활용하며, RDBMS 조인 및 DTO 매핑을 단순화합니다.                                                                                                                                |
| **`business_info`**     | **사용자 사업자 프로필 (1:N).** 업종, 지역, 매출액, 직원 수 등을 관리합니다. 유저가 여러 사업자를 가질 수 있으며, 대표 사업자(`is_default = true`)를 우선으로 AI 검수에 활용합니다.                                                                                        |
| **`programs`**          | **정부 지원사업 공고 마스터.** 외부 API에서 수집한 공고의 기본 정보(제목, 기관, 접수기간)와 수정일시 타임스탬프(`api_updated_at`)를 보관합니다. 마감된 공고도 삭제하지 않고 기간 조건으로 스크리닝합니다.                                                                  |
| **`program_documents`** | **공고 원문 및 AI 요약 (1:1 분리).** 무거운 원문 텍스트(`original_text`)와 LLM이 미리 가공한 요약문(`ai_summary`)을 보관하여 메인 검색 및 AI 프롬프트 Context 전달 속도를 극대화합니다.                                                                                    |
| **`reviews`**           | **AI 사전 검수 결과 및 이력.** 특정 사업자 기준 해당 공고의 신청 적합 여부(`status`: `PASS`, `FAIL`, `CHECK_REQUIRED`)와 세부 사유(`result_detail`)를 저장합니다. 검수 당시 유저 정보를 `business_snapshot (JSON)`으로 보존하여 프로필 수정 시 데이터 불일치를 방지합니다. |
| **`favorites`**         | **관심 공고 (즐겨찾기).** 유저가 관심 있어 하는 공고를 저장합니다. (유저-공고 간 다대다 관계 해소, Hard Delete 방식)                                                                                                                                                       |
| **`chat_rooms`**        | **AI 챗봇 대화 세션.** 챗봇 질문 대상이 되는 공고(`pblanc_id`)를 연결하여 대화 시 문맥을 제공합니다. 공고가 지워져도 대화 기록 보존을 위해 `ON DELETE SET NULL`을 사용합니다.                                                                                              |
| **`chat_messages`**     | **챗봇 대화 메시지 이력.** 유저(`USER`)와 AI 챗봇(`BOT`)이 주고받은 실제 대화 내용을 순서대로 보관합니다.                                                                                                                                                                  |

---

## 3. Core Architecture Rules for AI Coding Agent

AI 에이전트(Agentic CLI) 및 백엔드 개발자는 본 프로젝트의 코드를 작성하거나 수정할 때 아래 5가지 핵심 아키텍처 원칙을 반드시 준수해야 합니다.

1. **User Natural Key (Email) Rule:**
   - `users` 테이블의 Primary Key는 `email (VARCHAR(100))` 이다.
   - `business_info`, `reviews`, `favorites`, `chat_rooms` 등 유저 연관 테이블의 Foreign Key는 `user_id`가 아닌 `email`을 직접 참조한다. (`ON UPDATE CASCADE ON DELETE CASCADE`)

2. **Pre-computed Read Cache & Exception Rules (AI 호출 정책):**
   - **[기본 원칙 - 목록 조회 최적화]:** 단순 지원사업 목록 및 메인 화면 조회(`GET /api/programs`) 시 전체 공고에 대해 실시간 LLM(AI)을 일괄 호출하는 것은 절대 금지한다. DB에 미리 캐싱되어 있는 `reviews` 및 `program_documents` 데이터를 JOIN 조회하여 0.01초 내에 초고속 응답한다.
   - **[예외 A - 미검수 공고 발생 시]:** 특정 유저의 특정 공고에 대한 검수 데이터(`reviews`)가 존재하지 않는 경우:
     → 백엔드가 온디맨드/비동기(Background)로 AI 검수 API를 즉시 호출하여 `reviews` 테이블에 결과를 생성한 후 `status`를 서빙한다.
   - **[예외 B - 공고 수정 감지 및 재검수]:** 외부 API의 `updtPnttm` 필드가 변경되어 공고문이 수정되었거나 `reviews.is_outdated == TRUE`인 경우:
     → 백엔드 스케줄러(배치 작업) 또는 유저의 요청 시점에 최신 공고 텍스트를 기반으로 AI 요약(`program_documents`) 및 AI 재검수(`reviews`)를 새로 수행하여 DB를 최신화하고 `is_outdated = FALSE`로 복원한다.

3. **Snapshot Isolation for Reviews:**
   - `reviews` 테이블에는 검수 수행 당시의 유저 사업자 프로필을 `business_snapshot (JSON)` 형태로 반드시 함께 저장한다. 유저가 마이페이지에서 자기 프로필 정보(매출, 직원 수 등)를 수정해도 과거 검수 이력 데이터의 무결성이 파손되지 않도록 보호한다.

4. **Redis Separation Rule:**
   - SMS 인증번호(`phone_verifications`), JWT Refresh Token, 임시 검증 토큰은 RDBMS가 아닌 **Redis**에서 휘발성 데이터로 처리한다.

5. **UI Status Mapping Rule:**
   - `reviews.status` 컬럼은 다음 3가지 문자열만 허용하며, 프론트엔드 UI 뱃지 컬러와 1:1 매핑한다:
     - `'MATCHED'`: 신청 가능 (🟢 초록색)
     - `'UNMATCHED'`: 신청 불가 (🔴 빨간색)
     - `'NEED_CHECK'`: 조건 확인 필요 (🟡 노란색)

---

## 4. Database Schema (MariaDB DDL)

AI 에이전트는 JPA Entity 또는 DDL 생성 시 아래 SQL 스크립트를 원본 기준(Single Source of Truth)으로 사용해야 합니다.

```sql
-- 1. 사용자 테이블 (email이 PK)
CREATE TABLE users (
    email VARCHAR(100) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 사업자 정보 테이블 (email 참조, 1:N 다중 지원)
CREATE TABLE business_info (
    business_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    company_name VARCHAR(100),
    industry VARCHAR(50) NOT NULL,           -- 업종
    region VARCHAR(50) NOT NULL,             -- 지역
    business_period_months INT NOT NULL,     -- 사업 기간(월)
    employee_count INT NOT NULL,             -- 직원 수
    annual_revenue BIGINT NOT NULL,          -- 매출액
    is_default BOOLEAN DEFAULT TRUE,         -- 대표 사업자 여부
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_business_info_user FOREIGN KEY (email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 지원사업 공고 테이블 (updtPnttm 원본 저장용)
CREATE TABLE programs (
    pblanc_id VARCHAR(100) PRIMARY KEY,      -- 외부 공고 ID
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    organization VARCHAR(100),               -- 소관기관
    apply_start_date DATE,
    apply_end_date DATE,
    api_updated_at VARCHAR(30),              -- 외부 API의 updtPnttm 원본 타임스탬프
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 공고문 원문 및 AI 요약 (1:1 분리)
CREATE TABLE program_documents (
    pblanc_id VARCHAR(100) PRIMARY KEY,
    original_text LONGTEXT,                  -- 공고문 원문 TEXT
    ai_summary TEXT,                         -- AI 요약 TEXT
    doc_hash VARCHAR(64),                    -- 변경 감지용 Hash (SHA-256)
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_program_documents_program FOREIGN KEY (pblanc_id) REFERENCES programs(pblanc_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. AI 신청 전 검수 및 이력 (MariaDB JSON 타입 및 3가지 검수 상태)
CREATE TABLE reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    pblanc_id VARCHAR(100) NOT NULL,
    business_snapshot JSON NOT NULL,          -- 검수 당시 사업자 정보 스냅샷
    status VARCHAR(20) NOT NULL,             -- PASS(가능-초록), FAIL(불가-빨강), CHECK_REQUIRED(확인필요-노랑)
    result_detail JSON NOT NULL,             -- AI 세부 검수 결과/사유
    is_outdated BOOLEAN DEFAULT FALSE,       -- 공고 변경 시 스케줄러가 자동 재검수 처리
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reviews_program FOREIGN KEY (pblanc_id) REFERENCES programs(pblanc_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 관심 공고 (email 참조, Hard Delete 구조)
CREATE TABLE favorites (
    favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    pblanc_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_email_program UNIQUE (email, pblanc_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_favorites_program FOREIGN KEY (pblanc_id) REFERENCES programs(pblanc_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 챗봇 대화 세션 (질문 대상 공고 FK 포함)
CREATE TABLE chat_rooms (
    room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    pblanc_id VARCHAR(100) NULL,             -- 질문 대상 공고 ID (일반 질의 시 NULL)
    title VARCHAR(100) DEFAULT '지원사업 문의',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_rooms_user FOREIGN KEY (email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_chat_rooms_program FOREIGN KEY (pblanc_id) REFERENCES programs(pblanc_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 챗봇 대화 메시지 이력 (MariaDB ENUM 타입 적용)
CREATE TABLE chat_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_type ENUM('USER', 'BOT') NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms(room_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;