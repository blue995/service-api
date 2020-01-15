#!groovy

node {

    load "$JENKINS_HOME/jobvars.env"

    env.JAVA_HOME="${tool 'openjdk-11'}"
    env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"

    stage('JDK') {
        sh 'java -version'
        sh 'javac -version'
        sh 'echo $JAVA_HOME'
    }

    stage('Checkout') {
        checkout scm
    }
    stage('Test') {
        sh './gradlew clean test --full-stacktrace'
    }
    stage('Build') {
        sh './gradlew build'
    }
    stage('Docker image') {
        sh "./gradlew buildDocker -P dockerServerUrl=$DOCKER_HOST"
    }
    stage('Deploy container') {
        docker.withServer("$DOCKER_HOST") {
            sh "docker-compose -p reportportal51 -f $COMPOSE_FILE_RP_5_1 up -d --force-recreate api"
        }
    }
}