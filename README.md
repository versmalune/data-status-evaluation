# 공공데이터 제공운영 실태평가 시스템 - 2021 공공데이터포털 개선사업

* Environment
    * Java 1.8
    * SpringBoot 2.6.2
    * Cubrid
    * Spring-Data-JPA


* 개발환경
    * Intellij - Tyhmeleaf live reload
    * Run > Edit Configurations 수정
        * On 'Update': Update classes and resources (Tyhmeleaf live reload)
        * On frame deactivation: Update classes and resources (Tyhmeleaf live reload)
        * Active Profile: `dev`
    * DB migration
        * Cubrid에서는 flyway가 지원되지 않으므로, `resource/db/migration`폴더 이하에 DDL 관련 SQL 에 추가하여 관리
    * 개발환경 구동
    * [Install sass](https://sass-lang.com/install)
    ```sh
    # Homebrew
    brew install sass/sass/sass
    ```
    * Run application
    * run sass watch
      ```sh
      ./run-sass-watch.sh
      ```
    * 테스트용 Admin 접속계정
      > ID / PW : admin1 / admin


* Build
    * > cp ./src/main/resources/application-sample.yml ./src/main/resources/application.yml
    * > ./mvnw clean package -DskipTests


* Deploy
    * ```
      nohup java -jar -Dspring.profiles.active={profile} ./target/{jar} &
      ```
    * ```sh
      # 운영 서버 환경 배포
      1. 서비스 중지 : /home/jboss/dse/stop.sh
      
      2. 기존 jar 파일 삭제 및 신규 jar파일 위치 경로: /GCLOUD/Webapp/deploy/data-status-evaluation.jar
      
      3. jar 파일 복사해서 위 경로에 위치 ( jar 파일명을 data-status-evaluation.jar 로 해야함)
      
      4. 서비스 시작: /home/jboss/dse/start.sh
      
      5. 서버 기동 상태 및 로그 확인:  /home/jboss/dse/tail.sh
      ```
