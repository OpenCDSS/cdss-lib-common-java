package RTi.Util.IO;

import RTi.Util.IO.ProcessManager;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author iws
 */
public class ProcessManagerTest extends TestCase {

    public ProcessManagerTest(String testName) {
        super(testName);
    }

    public void testLotsOfOutput() throws InterruptedException {
        ProcessManager pm = processJavaTest("5000");
        pm.saveOutput(true);
        pm.run();
        assertEquals(5000,pm.getOutputList().size() );
        assertEquals(5000,pm.getErrorList().size() );
        assertEquals(0,pm.getExitStatus());
    }

    public void testArrayConstructorFailsWhenUsingInterpreter() {
        List<String> args = buildJavaArgs("1");
        ProcessManager pm = new ProcessManager(args.toArray(new String[0]));
        pm.run();
        assertTrue("expected non-zero exit code", pm.getExitStatus() != 0);
    }

    public void testStringConstructorWorksWithAdequateQuotingWhenUsingInterpreter() {
        List<String> args = buildJavaArgs("1");
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < args.size(); i++) {
            b.append(String.format("\"%s\"", args.get(i)));
            if (i + 1 < args.size()) {
                b.append(' ');
            }
        }
        b.append("\"");
        ProcessManager pm = new ProcessManager(b.toString());
        pm.run();
        assertEquals(0, pm.getExitStatus());
    }

    public void testProcessFails() {
        ProcessManager pm = processJavaTest();
        pm.saveOutput(true);
        pm.run();
        assertEquals(1,pm.getExitStatus());
    }

    public void testShellCommand() {
        ProcessManager pm = new ProcessManager("echo Hello > output.txt", 10000, null, true, getTempDir());
        pm.run();
        File expected = new File(getTempDir(),"output.txt");
        assertTrue(expected.exists());
        expected.delete();
    }

    public void testShellCommandProcessBuilder() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("cmd.exe","/c","echo Hello > output.txt");
        pb.redirectErrorStream();
        Process p = pb.start();
        InputStream in = p.getInputStream();
        byte[] b = new byte[1024];
        int r = 0;
        while ( (r = in.read(b)) > 0) {
            System.out.print(new String(b,0,r));
        }
        p.waitFor();
        File expected = new File("output.txt");
        assertTrue(expected.exists());
        expected.delete();
    }

    private File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
    
    private ProcessManager processJavaTest(String... args) {
        return process(buildJavaArgs(args).toArray(new String[0]));
    }

    private List<String> buildJavaArgs(String... args) {
        String java = findJava().getAbsolutePath();
        String cp = findTestClasses().getAbsolutePath() + File.pathSeparatorChar + findJunit().getAbsolutePath();
        ArrayList<String> argList = new ArrayList<String>();
        argList.add(java);
        argList.add("-cp");
        argList.add(cp);
        argList.add(getClass().getName());
        Collections.addAll(argList, args);
        return argList;
    }

    private ProcessManager process(String... args) {
        ProcessManager pm = new ProcessManager(args,0,null,false,new File("."));
        return pm;
    }

    private File findJava() {
        File f = new File(System.getProperty("java.home"), "bin/java.exe");
        if (!f.exists()) {
            throw new RuntimeException("Cannot locate java executable");
        }
        return f;
    }

    private File findJunit() {
        try {
            return new File(TestCase.class.getProtectionDomain().getCodeSource().getLocation().toURI().
                    getPath());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private File findTestClasses() {
        try {
            return new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().
                    getPath());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.exit(1);
        }
        int size = Integer.parseInt(args[0]);
        for (int i = 0; i < size; i++) {
            double rand = Math.random();
            for (int j = 0; j < 10; j++) {
                System.out.print(rand);
                System.err.print(rand);
            }
            System.out.println();
            System.err.println();
        }
    }
}
