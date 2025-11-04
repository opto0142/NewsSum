# NewsSum - AI 뉴스 요약 및 트렌드 분석 서비스

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.x-green.svg)](https://www.mongodb.com/)
[![Gemini API](https://img.shields.io/badge/Gemini%20API-1.x-orange.svg)](https://ai.google.dev/)

NewsSum은 Spring Boot 기반의 뉴스 크롤링 및 AI 요약 서비스입니다. Google Gemini API를 활용하여 다국어 뉴스를 한국어로 번역 및 요약하며, MongoDB에 데이터를 저장합니다.

## 🚀 주요 기능

### 🔐 사용자 인증 시스템
- JWT 기반 로그인/회원가입
- 역할 기반 권한 관리 (USER, PREMIUM, ADMIN)
- 프로모션 코드로 프리미엄 전환

### 📰 뉴스 크롤링 및 AI 요약
- Jsoup 기반 뉴스 크롤링 (Selenium fallback)
- Google Gemini API 연동 번역/요약
- URL 해시 기반 중복 방지
- 실시간 크롤링 상태 모니터링

### 👑 관리자 기능
- 프로모션 코드 생성/관리
- 사용자 통계 및 모니터링
- 시스템 설정 관리

## 🛠 기술 스택

### Backend
- **Java**: 17+
- **Framework**: Spring Boot 3.x
- **Security**: Spring Security + JWT
- **Database**: MongoDB (Spring Data MongoDB)
- **Crawling**: Jsoup
- **AI**: Google Gemini API (gemini-pro 모델)

### Frontend
- 순수 HTML/CSS/JavaScript (AI 생성)

### DevOps
- **Build**: Gradle (Groovy DSL)
- **Test**: JUnit 5, Spring Boot Test
- **CI/CD**: GitHub Actions

## 📋 요구사항

- Java 17 이상
- MongoDB 7.x
- Gradle Wrapper (동봉)
- Google Gemini API 키

## 🚀 설치 및 실행

### 1. 저장소 클론
```bash
git clone https://github.com/your-username/newssum.git
cd newssum
```

### 2. 환경 설정
```bash
# MongoDB 연결 (로컬)
spring.data.mongodb.uri=mongodb://localhost:27017/newssum

# Gemini API 키
gemini.api.key=${GEMINI_API_KEY}

# JWT 시크릿 (보안 키 생성)
jwt.secret=${JWT_SECRET}
```

### 3. 빌드 및 실행
```bash
# Gradle 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```

### 4. 확인
브라우저에서 `http://localhost:8080` 접속

## 📚 API 문서

### 인증 API
```
POST /api/auth/login       - 로그인
POST /api/auth/register    - 회원가입
POST /api/auth/refresh     - 토큰 갱신
```

### 프로모션 API (PREMIUM)
```
POST /api/promo/validate   - 프로모션 코드 검증
```

### 뉴스 API (PREMIUM)
```
POST /api/news/crawl       - 뉴스 크롤링 요청
GET  /api/news/list        - 크롤링 결과 목록
GET  /api/news/{id}        - 뉴스 상세 조회
```

### 관리자 API (ADMIN)
```
POST /api/admin/promo      - 프로모션 코드 생성
GET  /api/admin/users      - 사용자 목록
GET  /api/admin/stats      - 시스템 통계
```

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew check

# 테스트 커버리지 확인
./gradlew jacocoTestReport
```

## 🏗 프로젝트 구조

```
src/main/java/com/newssum/
├── config/           # 설정 클래스
├── controller/       # REST 컨트롤러
├── service/          # 비즈니스 로직
├── repository/       # 데이터 접근
├── domain/           # 도메인 모델
├── dto/              # 데이터 전송 객체
├── security/         # 보안 설정
├── crawler/          # 크롤링 컴포넌트
├── external/         # 외부 API 클라이언트
└── exception/        # 예외 처리
```

## 🔧 환경 설정

### 개발 환경
```yaml
# application-local.yml
spring:
  profiles:
    active: local
  data:
    mongodb:
      uri: mongodb://localhost:27017/newssum

gemini:
  api:
    key: ${GEMINI_API_KEY}
  model: gemini-pro
  temperature: 0.3
  max-output-tokens: 1000

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 3600000  # 1시간
  refresh-token-expiration: 604800000 # 7일
```

### 운영 환경
```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  data:
    mongodb:
      uri: ${MONGODB_URI}

gemini:
  api:
    key: ${GEMINI_API_KEY}
  timeout: 30s
  max-retries: 3

logging:
  level:
    com.newssum: INFO
```

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 개발 컨벤션
- [Java/Spring 코딩 컨벤션](.github/instructions/java-spring-coding.instructions.md)
- [API 설계 가이드](.github/instructions/api-design.instructions.md)
- [코드 리뷰 가이드라인](.github/instructions/review.instructions.md)

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 연락처

프로젝트 관리자: [Sunwoo Jang](mailto:newssum@dev.me.kr)

---

⭐ 이 프로젝트가 마음에 드시면 Star를 눌러주세요!