import com.alexperal.maven.MavenParser;
import com.alexperal.maven.MavenProject;
import com.alexperal.maven.MavenProjectId;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MavenParserTest {

    @Test
    public void testParseOutput() throws IOException {
        executeMavenParserFromFile(this::testMethod);
    }

    private void testMethod(ArrayBlockingQueue<String> queue) {
        MavenParser mavenParser = new MavenParser();
        MavenProject project = mavenParser.parse(queue);
        assertEquals(new MavenProjectId("com.alexperal.maventool","maventool","jar","1.0-SNAPSHOT"),project.getId());
        assertEquals(3, project.compileDeps().size());
    }

    private void executeMavenParserFromFile(Consumer<ArrayBlockingQueue<String>> cosumer) throws IOException {
        InputStream outputExample = MavenParserTest.class.getResourceAsStream("outputExample");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(outputExample))) {
            ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1000);
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.submit(() -> {
                try {
                    String s;
                    while ((s = bufferedReader.readLine()) != null) {
                        queue.put(s);
                    }
                    queue.put(MavenParser.poisonPill);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            executorService.shutdown();
            cosumer.accept(queue);
        }
    }


}