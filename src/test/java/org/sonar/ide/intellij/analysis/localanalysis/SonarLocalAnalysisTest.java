package org.sonar.ide.intellij.analysis.localanalysis;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.sonar.ide.intellij.listener.RefreshListener;
import org.sonar.ide.intellij.utils.SonarResourceKeyUtils;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 3/5/13
 * Time: 11:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class SonarLocalAnalysisTest extends LightCodeInsightFixtureTestCase {

    public static final String POMIDORO_SAMPLE = "pomidoro_sample";
    private SonarLocalAnalysis sonarLocalAnalysis;
    private PsiClass abcClass;
    private VirtualFile abcClassVirtualFile;
    private VirtualFile perfectClass;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        populateProject(POMIDORO_SAMPLE);

        JSONObject parsedValue = (JSONObject) JSONValue.parse(new FileReader(findFile("dryRun.json")));
        final JSONObject violationByResources = (JSONObject) parsedValue.get("violations_per_resource");
        sonarLocalAnalysis = new SonarLocalAnalysis(getProject(), violationByResources);

        abcClass = myFixture.findClass("ru.greeneyes.project.pomidoro.Abc");
        abcClassVirtualFile = abcClass.getContainingFile().getVirtualFile();
        PsiClass perfectClass = myFixture.findClass("ru.greeneyes.project.pomidoro.PerfectClass");
        this.perfectClass = perfectClass.getContainingFile().getVirtualFile();

        SonarResourceKeyUtils.getSonarModuleComponent(getProject(), abcClassVirtualFile).getState().configured = true;
    }

    private URL populateProject(String projectName) throws URISyntaxException, IOException {
        URL project = ClassLoader.getSystemResource("testData/" + projectName);

        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(project.toURI()), new String[]{"java.txt"}, true);
        while (fileIterator.hasNext()) {
            File next = fileIterator.next();
            myFixture.addClass(IOUtils.toString(next.toURI()));
        }

        return project;
    }

    public void testGetSource() throws Exception {
        Source source = sonarLocalAnalysis.getSource(abcClassVirtualFile);
        assertSourceCodeMatchesTheClass(abcClass, source);
    }

    private void assertSourceCodeMatchesTheClass(PsiClass aClass, Source source) {
        assertNotNull(source);
        Collection<String> lines = source.getLines();
        String[] sourceClassLines = aClass.getContainingFile().getText().split("\n");
        assertEquals(sourceClassLines.length, lines.size());
        Iterator<String> iterator = lines.iterator();
        for (String line : sourceClassLines) {
            String targetFileLines = iterator.next();
            assertEquals(targetFileLines, line);

        }
    }


    public void testGetViolation() throws Exception {

        final List<Violation> expectedViolations = buildExpectedViolationList();

        List<Violation> currentViolations = sonarLocalAnalysis.getViolations(abcClassVirtualFile);
        assertNotNull(currentViolations);
        assertEquals(4, currentViolations.size());

        assertExpectTheSameViolations(expectedViolations, currentViolations);

    }

    private void assertExpectTheSameViolations(List<Violation> expectedViolations, List<Violation> currentViolations) {
        Iterator<Violation> iterator = expectedViolations.iterator();
        for (Violation violation : currentViolations) {
            Violation expectedViolation = iterator.next();
            assertEquals(expectedViolation.getMessage(), violation.getMessage());
        }
    }

    private List<Violation> buildExpectedViolationList() {

        JSONObject expectedRawViolation = (JSONObject) JSONValue.parse("{\"ru.greeneyes.project.pomidoro.Abc\":[{\"line\":13,\"message\":\"Avoid using if statements without curly braces\",\"severity\":\"MAJOR\",\"rule_key\":\"IfStmtsMustUseBraces\",\"rule_repository\":\"pmd\",\"rule_name\":\"If Stmts Must Use Braces\"},{\"line\":13,\"message\":\"Do not use if statements that are always true or always false\",\"severity\":\"CRITICAL\",\"rule_key\":\"UnconditionalIfStatement\",\"rule_repository\":\"pmd\",\"rule_name\":\"Unconditional If Statement\"},{\"line\":15,\"message\":\"System.out.print is used\",\"severity\":\"MAJOR\",\"rule_key\":\"SystemPrintln\",\"rule_repository\":\"pmd\",\"rule_name\":\"System Println\"},{\"line\":18,\"message\":\"Avoid unused private methods such as 'fake()'.\",\"severity\":\"MAJOR\",\"rule_key\":\"UnusedPrivateMethod\",\"rule_repository\":\"pmd\",\"rule_name\":\"Unused private method\"}]}");
        JSONArray violations = (JSONArray) expectedRawViolation.get("ru.greeneyes.project.pomidoro.Abc");
        List<Violation> expectedViolation = new ArrayList<Violation>();
        for (Object violationObj : violations) {
            Violation violation;
            JSONObject jsonViolation = (JSONObject) violationObj;
            violation = WSViolationUnMarshaller.unMarshallViolation(jsonViolation);

            expectedViolation.add(violation);
        }
        return expectedViolation;
    }

    public void testCleanup() throws Exception {
        assertNotNull(sonarLocalAnalysis.getSource(abcClassVirtualFile));
        assertNotNull(sonarLocalAnalysis.getViolations(abcClassVirtualFile));
        sonarLocalAnalysis.clear();
        assertEquals(0, sonarLocalAnalysis.getSourceSet().size());
        assertEquals(0, sonarLocalAnalysis.getViolationSet().size());
    }

    public void testLoadViolation() throws Exception {

        final List<Boolean> done = new ArrayList<Boolean>();
        sonarLocalAnalysis.loadViolations(abcClassVirtualFile, new RefreshListener<Violation>() {
            @Override
            public void doneRefresh(VirtualFile virtualFile, List<Violation> violationList) {
                assertEquals(abcClassVirtualFile, virtualFile);
                assertEquals(4, violationList.size());
                assertExpectTheSameViolations(buildExpectedViolationList(), violationList);
                done.add(Boolean.TRUE);
            }
        });

        assertEquals(1, done.size());
        assertEquals(Boolean.TRUE, done.get(0));

    }

    public void testGetNotViolatedResource() throws Exception {
        List<Violation> violations = sonarLocalAnalysis.getViolations(perfectClass);
        assertNotNull(violations);
        assertEquals(0, violations.size());
    }

    private String findFile(String fileName) {
        return ClassLoader.getSystemResource("testData/" + POMIDORO_SAMPLE + "/" + fileName).getFile();
    }


}
