package io.github.divinespear.maven.plugin;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class Issue10Test
        extends AbstractSchemaGeneratorMojoTest {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Simple schema generation test for script using Hibernate
     * 
     * @throws Exception
     */
    @Test
    public void testIssue10() throws Exception {
        final File pomfile = this.getPomFile("target/test-classes/unit/issue-10");

        this.compileJpaModelSources(pomfile);
        JpaSchemaGeneratorMojo mojo = this.executeSchemaGeneration(pomfile);

        File createScriptFile = mojo.getCreateOutputFile();
        assertThat("create script should be generated.", createScriptFile.exists(), is(true));

        final String expectCreate = readResourceAsString("/unit/hibernate-simple-script-test/expected-create.txt");
        assertThat(this.readFileAsString(createScriptFile), is(expectCreate));

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        final String expectDrop = readResourceAsString("/unit/hibernate-simple-script-test/expected-drop.txt");
        assertThat(this.readFileAsString(dropScriptFile), is(expectDrop));
    }
}
