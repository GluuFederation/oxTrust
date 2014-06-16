package org.gluu.oxtrust.action.test;

import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class RemovePersonTest extends AbstractAuthorizationTest {

    @BeforeTest
    public void initTestConfiguration() throws Exception {
        initTest();
    }

    @Test
    @Parameters({"userPropsKey", "removePropsKey"})
    public void testRemovePerson(String userPropsKey, String removePropsKey) throws Exception {
        loginAndCheckLoggedInFacesRequest(userPropsKey);
        removePerson(removePropsKey);
    }

    private void removePerson(final String removePropsKey) throws Exception {

        new FacesRequest("/admin/person/update"){
            protected void updateModelValues() throws Exception {
                setValue("#{updatePersonAction.inum}", getConf().getString(removePropsKey + ".inum"));
                invokeAction("#{updatePersonAction.update}");
            }

            protected void invokeApplication()throws Exception {
                invokeAction("#{updatePersonAction.delete}");
            }

            protected void renderResponse() throws Exception {
                PersonService personService = (PersonService)getInstance("personService");
                GluuCustomPerson person = null;
                try{
                    person = personService.getPersonByInum(getConf().getString(removePropsKey + ".inum"));
                } catch (LdapMappingException ex){
                    assert true;
                }
                assert person == null : "Person hasn't been removed";
            }

        }.run();
    }

}
