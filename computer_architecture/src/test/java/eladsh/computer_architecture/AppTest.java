package eladsh.computer_architecture;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AppTest {

	@Parameters
    public static Collection<Object[]> data() {
		List<Object[]> dirs = new LinkedList<Object[]>();
			URL url = AppTest.class.getResource("resources/");
			File resourcesDir;
			try {
			  resourcesDir = new File(url.toURI());
			} catch(URISyntaxException e) {
			  resourcesDir = new File(url.getPath());
			}
			
			for (File dir: resourcesDir.listFiles()) {
				if (!dir.isDirectory() || !dir.getName().startsWith("test")) {
					continue;
				}
				dirs.add(new Object[]{dir});
			}
			return dirs;
    }
    
    @BeforeClass
    public static void initGlobalResources() throws IOException {
    	df = new SimpleDateFormat("HH-mm");
    	tempDir = Files.createTempDirectory(Paths.get("work/temp"), "out_"+df.format(new Date())+"_").toFile();
    }
    
    @AfterClass
    public static void closeGlobalResources() throws IOException {
    	FileUtils.deleteDirectory(tempDir);
    }

    private static DateFormat df;
    private static File tempDir;
    private final File inDir;
    
    public AppTest(File inDir) {
    	this.inDir = inDir;
    }
	
	@Test
	public void testDir() throws IOException {
		File outDir = new File(tempDir, this.inDir.getName());
		
		File cfgFile = new File(this.inDir, "cfg.txt");
		File memInFile = new File(this.inDir, "memin.txt");
		
		File expectedMemOutFile = new File(this.inDir, "memout.txt");
		File expectedRegOut0File = new File(this.inDir, "regout0.txt");
		File expectedRegOut1File = new File(this.inDir, "regout1.txt");
		File expectedTrace0File = new File(this.inDir, "trace0.txt");
		File expectedTrace1File = new File(this.inDir, "trace1.txt");
		File expectedCpi0File = new File(this.inDir, "cpi0.txt");
		File expectedCpi1File = new File(this.inDir, "cpi1.txt");
		
		File memOutFile = new File(outDir, "memout.txt");
		File regOut0File = new File(outDir, "regout0.txt");
		File regOut1File = new File(outDir, "regout1.txt");
		File trace0File = new File(outDir, "trace0.txt");
		File trace1File = new File(outDir, "trace1.txt");
		File cpi0File = new File(outDir, "cpi0.txt");
		File cpi1File = new File(outDir, "cpi1.txt");
		
		new App(cfgFile, memInFile, memOutFile, regOut0File, trace0File, cpi0File, regOut1File, trace1File, cpi1File);
		
		compareFiles(expectedMemOutFile, memOutFile);
		compareFiles(expectedRegOut0File, regOut0File);
		compareFiles(expectedRegOut1File, regOut1File);
		compareFiles(expectedTrace0File, trace0File);
		compareFiles(expectedTrace1File, trace1File);
		compareFiles(expectedCpi0File, cpi0File);
		compareFiles(expectedCpi1File, cpi1File);
		
	}
	
	private void compareFiles(File expected, File collected) throws IOException {
		String expectedStr = FileUtils.readFileToString(expected, "utf-8").toLowerCase();
		String collectedStr = FileUtils.readFileToString(collected, "utf-8").toLowerCase();
		try {
			Assert.assertEquals(this.inDir.getName()+" "+collected.getName()+": expected differs from collected!", expectedStr, collectedStr); 
		} catch (AssertionError e) {
			throw e;
		}
		
	}
}
