# 지원UP Frontend

1인 소상공인의 사업정보와 정부지원사업 공고 조건을 비교해 신청 전 확인을 돕는 서비스의 프론트엔드 기준 프레임워크입니다.

## 실행

```bash
npm install
copy .env.example .env
npm run dev
```

품질 검사는 다음 명령으로 실행합니다.

```bash
npm run lint
npm run build
```

## 인증 기준

- `/login`, `/signup`만 비로그인 상태에서 접근할 수 있습니다.
- `/home`, `/programs`, `/programs/:pblancId`, AI 검수, 마이페이지는 로그인 후에만 접근할 수 있습니다.
- 현재는 `AuthContext`와 `localStorage`의 mock token으로 흐름을 확인합니다.
- 백엔드 연동 시 `authApi`의 응답을 `AuthContext`에 연결하면 됩니다.

## 폴더 역할

- `src/api`: Axios 인스턴스와 도메인별 API 함수
- `src/components/common`: 공통 UI와 레이아웃
- `src/components/*`: 기능별 재사용 컴포넌트
- `src/pages`: 라우트 단위 화면
- `src/routes`: 공개 및 보호 라우트
- `src/context`, `src/hooks`: 인증 상태와 공통 훅
- `src/mocks`: 백엔드 연결 전 화면 확인용 데이터
- `src/styles`: 디자인 토큰, 공통, 기능별 스타일

## 협업 기준

기능 개발은 `feature/*` 브랜치에서 진행하고 `develop`으로 Pull Request를 생성합니다. 공통 컴포넌트 변경은 사용처를 먼저 확인하고, 기능별 로직과 스타일은 해당 도메인 폴더에 둡니다.
