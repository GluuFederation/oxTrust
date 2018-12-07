package org.gluu.oxtrust.api.custom.script.test;

import org.gluu.oxtrust.action.test.BaseTest;
import org.gluu.oxtrust.api.customscripts.CustomScriptDTO;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xdi.model.AuthenticationScriptUsageType;
import org.xdi.model.ProgrammingLanguage;
import org.xdi.model.ScriptLocationType;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.model.custom.script.model.ScriptError;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.testng.Assert.*;

/**
 * Test class for  custom script management service
 *
 * @version 10 September, 2018
 * @Author Shoeb Khan
 */

public class CustomScriptMgmtSvcTest extends BaseTest {

    public static final String API_URL = OxTrustApiConstants.BASE_API_URL + "/configurations/scripts";

    private CustomScriptDTO lastSavedScript;

    /**
     * Test case for getting the registration configuration info, without search pattern
     *
     * @param webTarget
     */
    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Test
    public void getAllScripts(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {
        Response response = null;
        try {
            final Invocation.Builder builder = prepareBuilder(webTarget, API_URL);
            response = builder.get();

            assertEquals(response.getStatus(), 200);

            final Map<CustomScriptType, List<CustomScript>> scriptTypeListMap = response.readEntity(Map.class);
            assertNotNull(scriptTypeListMap);

        } finally {

            cleanUp(response);

        }
    }

    /**
     * Tests script saving functionality when it's name is invalid
     *
     * @param webTarget
     */
    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Test
    public void testSaveScriptWithInvalidName(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {

        Response response = null;
        try {
            final Invocation.Builder builder = prepareBuilder(webTarget, API_URL);

            final CustomScriptDTO script = new CustomScriptDTO();
            script.setModuleProperties(new ArrayList<SimpleCustomProperty>());
            script.setLocationPath("");
            script.setName("test-script");
            script.setProgrammingLanguage(ProgrammingLanguage.PYTHON.getValue());

            final Entity<CustomScriptDTO> entity = Entity.entity(script, APPLICATION_JSON_TYPE);
            response = builder.buildPut(entity).invoke();

            assertNotNull(response);
            assertEquals(response.getStatus(), 400);

        } finally {

            cleanUp(response);

        }

    }

    /**
     * Tests script saving functionality when programming language is not set
     */
    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Test
    public void testSaveScriptWithNoPL(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {

        Response response = null;

        try {
            final Invocation.Builder builder = prepareBuilder(webTarget, API_URL);

            final CustomScriptDTO script = new CustomScriptDTO();
            script.setModuleProperties(new ArrayList<SimpleCustomProperty>());
            script.setLocationPath("");
            script.setName("test_script_123");
            script.setProgrammingLanguage("");

            final Entity<CustomScriptDTO> entity = Entity.entity(script, APPLICATION_JSON_TYPE);
            final Invocation invocation = builder.buildPut(entity);
            response = invocation.invoke();

            assertNotNull(response);

            assertEquals(response.getStatus(), 400);

        } finally {

            cleanUp(response);
        }

    }

    /**
     * Tests custom script's type change functionality
     */
    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Test
    public void testUpdateScriptsType(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {
        Response response = null;

        try {

            testSaveScript(webTarget);

            final Invocation.Builder builder = prepareBuilder(webTarget, API_URL);

            lastSavedScript.setScriptType(CustomScriptType.CACHE_REFRESH.getValue());

            final Entity<CustomScriptDTO> entity = Entity.entity(lastSavedScript, APPLICATION_JSON_TYPE);
            response = builder.buildPut(entity).invoke();

            assertNotNull(response);
            assertEquals(response.getStatus(), 400);

        } finally {

            cleanUp(response);

        }
    }

    /**
     * Tests script saving functionality
     */
    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Test
    public void testSaveScriptWithInvalidLocation(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {

        Response response = null;

        try {

            final CustomScriptDTO script = new CustomScriptDTO();
            addValidDataToScript(script);
            script.setLocationType(null);  // invalid location

            response = saveScript(webTarget, script);

            assertNotNull(response);
            assertEquals(response.getStatus(), 400);

        } finally {

            cleanUp(response);

        }

    }

    /**
     * Tests script saving functionality
     */
    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Test
    public void testSaveScript(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {

        Response response = null;

        try {

            final CustomScriptDTO script = new CustomScriptDTO();

            addValidDataToScript(script);

            response = saveScript(webTarget, script);

            assertNotNull(response);
            assertEquals(response.getStatus(), 200);

            CustomScriptDTO resultScript = response.readEntity(CustomScriptDTO.class);
            lastSavedScript = resultScript;

            assertTrue(resultScript.getInum() != null);

        } finally {

            cleanUp(response);

        }

    }

    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Test
    public void testRemoveScriptWithInvalidId(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {

        Response response = null;
        try {

            final Invocation.Builder builder = prepareBuilder(webTarget, API_URL + "/blah-blah");

            response = builder.delete();

            assertEquals(response.getStatus(), 404);

        } finally {

            cleanUp(response);

        }

    }

    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Test (dependsOnMethods = "testScriptError")
    public void testGetAllErrors(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {

        Response response = null;

        try {
            final String url = API_URL + "/errors";
            final Invocation.Builder builder = prepareBuilder(webTarget, url);

            response = builder.buildGet().invoke();

            assertEquals(response.getStatus() , 200);

            final List<CustomScriptDTO> scriptList = response.readEntity(new GenericType<List<CustomScriptDTO>>(){});

            assertNotNull(scriptList);
            assertTrue(scriptList.size() > 0);
            assertNotNull(scriptList.get(0).getScriptError() , null);

        } finally {

            cleanUp(response);
        }

    }

    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Test
    public void testScriptError(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {
        if (lastSavedScript == null) {
            saveScriptWithErrorStack(webTarget);
        }

        final String url = API_URL + "/errors/" + lastSavedScript.getInum();

        Response response = null;

        try {

            final Invocation.Builder builder = prepareBuilder(webTarget, url);
            builder.accept(MediaType.APPLICATION_JSON);

            response = builder.buildGet().invoke();

            final ScriptError scriptError = response.readEntity(ScriptError.class);
            assertNotNull(scriptError);

        } finally {

            cleanUp(response);
        }
    }


    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Test
    public void testRemoveScript(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) {

        if (lastSavedScript == null) {
            testSaveScript(webTarget);
        }

        final CustomScriptDTO customScript = lastSavedScript;

        final String inum = customScript.getInum();

        Response response = null;
        try {

            final String url = API_URL + "/" + inum;
            final Invocation.Builder builder = prepareBuilder(webTarget, url);

            response = builder.delete();

            assertEquals(response.getStatus(), 200);

        } finally {

            cleanUp(response);
        }

    }

    private void saveScriptWithErrorStack(WebTarget webTarget) {
        Response response = null;
        try {
            final CustomScriptDTO script = new CustomScriptDTO();
            final ScriptError scriptError = new ScriptError();
            final Exception exception = new Exception("Test exception");
            final StringWriter writer = new StringWriter();

            exception.printStackTrace(new PrintWriter(writer));

            scriptError.setStackTrace(writer.toString());

            script.setScriptError(scriptError);
            addValidDataToScript(script);

            response = saveScript(webTarget, script);

            lastSavedScript = response.readEntity(CustomScriptDTO.class);

        } finally {

            cleanUp(response);

        }

    }


    private Response saveScript(WebTarget webTarget, CustomScriptDTO script) {

        final Invocation.Builder builder = prepareBuilder(webTarget, API_URL);

        final Entity<CustomScriptDTO> entity = Entity.entity(script, APPLICATION_JSON_TYPE);

        return builder.buildPut(entity).invoke();

    }

    private static void addValidDataToScript(final CustomScriptDTO script) {

        script.setName("test_script_234");
        script.setScriptType(CustomScriptType.PERSON_AUTHENTICATION.getValue());
        script.setProgrammingLanguage(ProgrammingLanguage.PYTHON.getValue());
        script.setUsageType(AuthenticationScriptUsageType.INTERACTIVE);
        script.setLocationType(ScriptLocationType.LDAP.getValue());
    }


    private Invocation.Builder prepareBuilder(final WebTarget webTarget, final String apiUrl) {
        return webTarget.path(apiUrl).request().accept(APPLICATION_JSON_TYPE);
    }

    private static void cleanUp(Response response) {

        if (response != null) {
            response.close();
        }
    }


}