def runTestSuite(delivery, environment = 'dev', categories = null) {
  def categoryProp = categories == null ? '' : "-Dgroups='${categories}'"

  try {

    delivery.dockerLogin()
    docker.image('ssartisan/pints-mvn:1.0').inside {
      sh "mvn -Dserver.environment=${environment} ${categoryProp} clean test"
    }
    delivery.notify('good', "(<${env.BUILD_URL}|Job>) `${JOB_NAME}:${env.BUILD_NUMBER}`\nEnd-to-end tests passed.")
  }
  catch (e) {
    currentBuild.result = "FAILED"
    delivery.notifyFailure(e.getMessage())
    throw e
  }
  finally {
    junit 'target/surefire-reports/*.xml'
  }
}

def runProdSuite(delivery, environment = 'dev', categories = null) {
  def categoryProp = categories == null ? '' : "-Dgroups='${categories}'"

  try {
    docker.image('ssartisan/pints-mvn:1.0').inside {
      sh "mvn -Dserver.environment=${environment} -Dtest=ProductionSanityTest ${categoryProp} clean test"
    }
    delivery.notify('good', "(<${env.BUILD_URL}|Job>) `${JOB_NAME}:${env.BUILD_NUMBER}`\nEnd-to-end tests passed.")
  }
  catch (e) {
    currentBuild.result = "FAILED"
    delivery.notifyFailure(e.getMessage())
    throw e
  }
  finally {
    junit 'target/surefire-reports/*.xml'
  }
}

return this
