package jadx.tests.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;

import jadx.api.JadxDecompiler;
import jadx.core.dex.nodes.ClassNode;

public abstract class SmaliTest extends IntegrationTest {

	private static final String SMALI_TESTS_PROJECT = "jadx-core";
	private static final String SMALI_TESTS_DIR = "src/test/smali";
	private static final String SMALI_TESTS_EXT = ".smali";

	protected ClassNode getClassNodeFromSmali(String file, String clsName) {
		File smaliFile = getSmaliFile(file);
		File outDex = createTempFile(".dex");
		compileSmali(outDex, Collections.singletonList(smaliFile));
		return getClassNodeFromFile(outDex, clsName);
	}

	protected ClassNode getClassNodeFromSmali(String clsName) {
		return getClassNodeFromSmali(clsName, clsName);
	}

	protected ClassNode getClassNodeFromSmaliWithPath(String path, String clsName) {
		return getClassNodeFromSmali(path + File.separatorChar + clsName, clsName);
	}

	protected ClassNode getClassNodeFromSmaliWithPkg(String pkg, String clsName) {
		return getClassNodeFromSmali(pkg + File.separatorChar + clsName, pkg + '.' + clsName);
	}

	protected ClassNode getClassNodeFromSmaliFiles(String pkg, String testName, String clsName) {
		File outDex = createTempFile(".dex");
		compileSmali(outDex, collectSmaliFiles(pkg, testName));
		return getClassNodeFromFile(outDex, pkg + "." + clsName);
	}

	protected JadxDecompiler loadSmaliFile(String pkg, String smaliFileName) {
		File outDex = createTempFile(".dex");
		compileSmali(outDex, Collections.singletonList(getSmaliFile(pkg + File.separatorChar + smaliFileName)));
		return loadFiles(Collections.singletonList(outDex));
	}

	protected JadxDecompiler loadSmaliFiles(String pkg, String testNameDir) {
		File outDex = createTempFile(".dex");
		compileSmali(outDex, collectSmaliFiles(pkg, testNameDir));
		return loadFiles(Collections.singletonList(outDex));
	}

	private List<File> collectSmaliFiles(String pkg, @Nullable String testDir) {
		String smaliFilesDir;
		if (testDir == null) {
			smaliFilesDir = pkg + File.separatorChar;
		} else {
			smaliFilesDir = pkg + File.separatorChar + testDir + File.separatorChar;
		}
		File smaliDir = new File(SMALI_TESTS_DIR, smaliFilesDir);
		String[] smaliFileNames = smaliDir.list((dir, name) -> name.endsWith(".smali"));
		assertThat("Smali files not found", smaliFileNames, notNullValue());
		return Arrays.stream(smaliFileNames)
				.map(file -> new File(smaliDir, file))
				.collect(Collectors.toList());
	}

	private static File getSmaliFile(String baseName) {
		File smaliFile = new File(SMALI_TESTS_DIR, baseName + SMALI_TESTS_EXT);
		if (smaliFile.exists()) {
			return smaliFile;
		}
		File pathFromRoot = new File(SMALI_TESTS_PROJECT, smaliFile.getPath());
		if (pathFromRoot.exists()) {
			return pathFromRoot;
		}
		throw new AssertionError("Smali file not found: " + smaliFile.getPath());
	}

	private static boolean compileSmali(File output, List<File> inputFiles) {
		try {
			SmaliOptions params = new SmaliOptions();
			params.outputDexFile = output.getAbsolutePath();
			List<String> inputFileNames = inputFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList());
			Smali.assemble(params, inputFileNames);
		} catch (Exception e) {
			throw new AssertionError("Smali assemble error", e);
		}
		return true;
	}
}
