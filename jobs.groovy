def gitURL = "https://github.com/MNT-Lab/mntlab-dsl.git"
def command = "git ls-remote -h $gitURL"
def proc = command.execute()
proc.waitFor()
if ( proc.exitValue() != 0 ) { 
  println "Error, ${proc.err.text}" 
  System.exit(-1)
}

def repobr = proc.in.text.readLines().collect { 
  it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
}


job("EPBYMINW3093/MNTLAB-asemirski-main-build-job") {
		scm {
       			github 'MNT-Lab/mntlab-dsl', '$BRANCH_NAME'
 		 }
	       	 parameters {
   			 choiceParam('BRANCH_NAME', ['asemirski', 'master'], 'Choose branch')
  		 }
		for (i = 1; i <2; i++) {
 			 job("EPBYMINW3093/MNTLAB-asemirski-child${i}-build-job") {
				scm {
       					github 'MNT-Lab/mntlab-dsl', 'asemirski'
    				}
				steps {
        				shell('chmod +x ./script.sh > output.txt ./script.sh')
       				 }
				parameters {choiceParam("BRANCH_NAME", repobr,'Choose branch')}	
			}
		}
}
