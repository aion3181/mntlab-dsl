def gitrepo = 'MNT-Lab/mntlab-dsl'
def branchname = 'vtarasiuk'

/** Geting list of branches*/
def gitURL = "https://github.com/MNT-Lab/mntlab-dsl.git"
def command = "git ls-remote -h $gitURL"

def proc = command.execute()
proc.waitFor()

if ( proc.exitValue() != 0 ) {
    println "Error, ${proc.err.text}"
    System.exit(-1)
}

def branches = proc.in.text.readLines().collect {
    it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
}

    /** Name Section **/

/** Setting list of master parameters  (hardcode)*/
String student = 'vtarasiuk'; String master = 'master'
def masterchoice = [student, master]

/** Setting master-job name*/
def lord = 'MNTLAB-vtarasiuk-main-build-job'

/** Common Folder name*/
def folder = 'EPBYMINW2471'

/** Setting list of child job names  (hardcode)*/
def jbn = []
for (i in 1..4){
    jbn.add("MNTLAB-vtarasiuk-child${i}-build-job")
}

    /**Job Section**/

/** Create Master job*/
job("${folder}/${lord}") {
    parameters {
        choiceParam('BRANCH_NAME', masterchoice)
        activeChoiceParam('JOB_NAME') {
            choiceType('CHECKBOX')
            groovyScript {
                script("return ['1', '2', '3', '4']")
            }
        }
    }
    scm {
        github(gitrepo, branchname)
    }
    triggers {
        scm('H/5 * * * *')
    }

    steps {
        downstreamParameterized {
            trigger(jbn) {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }


                parameters {
                    currentBuild()
                }
            }
        }
    }

}
/** Create child jobs*/
jbn.each {
    job("${folder}/${it}") {
        parameters {
            choiceParam('BRANCH_NAME', branches)
        }
        scm {
            github(gitrepo, branchname)
        }
        triggers {
            scm('H/5 * * * *')
        }
        steps {

            shell ('chmod +x script.sh && ./script.sh > output.txt && cat output.txt')
            shell ('tar -czf ${BRANCH_NAME}_dsl_script.tar.gz output.txt jobs.groovy script.sh')
        }
        publishers {
            archiveArtifacts('output.txt')
            archiveArtifacts('*_dsl_script.tar.gz')
        }
    }
}


