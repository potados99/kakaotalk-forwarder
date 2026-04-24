# KakaoTalk Forwarder 브레인스톰

**날짜:** 2026-03-30
**상태:** 확정

## 무엇을 만드는가

카카오톡 알림을 감지하여 특정 발신자의 메시지를 외부 API로 자동 전달하는 Android 앱.

**구체적 시나리오:** 카카오톡에서 "진한식당m"이라는 닉네임의 알림이 오면, 알림 본문을 `slack-lunch-fairy`의 `POST /api/menuPosts`로 전송하여 슬랙 채널에 점심 메뉴가 자동 게시되도록 한다.

## 왜 이 앱이 필요한가

현재 `slack-lunch-fairy`는 외부에서 메뉴를 받아 슬랙에 뿌리는 API를 갖추고 있지만, 메뉴를 자동 수집하는 수단이 없다. 카카오톡 단체방에서 매일 올라오는 식당 메뉴를 수동으로 복붙하지 않고 자동 전달하기 위함.

## 핵심 구성 요소

### 1. NotificationListenerService

- Android의 `NotificationListenerService`를 상속하여 카카오톡(`com.kakao.talk`) 알림을 감지
- 알림의 `title`이 설정된 닉네임(기본값: "진한식당m")과 일치할 때만 동작
- 알림의 `text`(본문)를 `menuText`로 사용

### 2. HTTP 전송

- **Endpoint:** 설정에서 입력한 API URL + `/api/menuPosts`
- **Method:** POST
- **Headers:** `Authorization: Bearer {설정에서 입력한 토큰}`, `Content-Type: application/json`
- **Body:** `{ "menuText": "<알림 본문>", "source": "kakaotalk" }`
- source는 `"kakaotalk"` 고정

### 3. 화면 구성 (탭 2개)

**탭 1 - 설정:**
- API 서버 URL (텍스트 입력)
- Bearer 토큰 (텍스트 입력)
- 감지할 닉네임 (텍스트 입력, 기본값 "진한식당m")
- 알림 접근 권한 상태 표시 및 설정 이동 버튼

**탭 2 - 이력:**
- 포워딩 이력 목록 (최신순)
- 각 항목: 시간, 본문 미리보기, 성공/실패 상태
- 실패 항목에 재시도 버튼
- 재시도 성공 시 성공으로 상태 변경

### 4. 로컬 저장소

- **설정값:** SharedPreferences (API URL, 토큰, 닉네임)
- **포워딩 이력:** Room DB (시간, 본문, 성공 여부, 서버 응답)

## 주요 결정 사항

| 결정 | 선택 | 이유 |
|------|------|------|
| 알림 본문 처리 | 가공 없이 그대로 전송 | 알림 본문에 이미 날짜 포함 |
| source 필드 | `"kakaotalk"` 고정 | 별도 설정 불필요 |
| 실패 처리 | 이력 목록 + 재시도 버튼 | 자동 재시도 없이 사용자가 확인 후 수동 재시도 |
| 화면 구성 | 탭 2개 (설정 / 이력) | 설정과 이력을 깔끔하게 분리 |
| UI 프레임워크 | Jetpack Compose + Material3 | 이미 프로젝트에 설정됨 |

## 기술 스택

- **UI:** Jetpack Compose, Material3
- **알림 감지:** NotificationListenerService
- **HTTP:** Ktor Client 또는 OkHttp
- **로컬 DB:** Room
- **설정 저장:** SharedPreferences (또는 DataStore)
