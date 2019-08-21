import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class main {

    private static void printResults(InvocationResult result){
        System.out.println(result);
    }
    public static void main(String[] args) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File( args[0] ) );
        request.setGoals( Collections.singletonList( "dependency:tree -Dverbose" ) );


        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(args[1]));
        invoker.setOutputHandler(System.out::println);
        InvocationResult result = invoker.execute( request );

        if ( result.getExitCode() != 0 )
        {
            throw new IllegalStateException( "Build failed." );
        }
        printResults(result);
    }
}
