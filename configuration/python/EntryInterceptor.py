from org.gluu.oxtrust.ldap.cache.service.intercept.interfaces import EntryInterceptorType
from org.xdi.util import StringHelper
from org.gluu.oxtrust.model import GluuCustomPerson
from org.gluu.oxtrust.model import GluuCustomAttribute

import java

class EntryInterceptor(EntryInterceptorType):
    def __init__(self, currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis

    def updateAttributes(self, person):
        attributes = person.getCustomAttributes()

        # Add new attribute preferredLanguage
        attrPrefferedLanguage = GluuCustomAttribute("preferredLanguage", "en-us")
        attributes.add(attrPrefferedLanguage)

        # Add new attribute userPassword
        attrUserPassword = GluuCustomAttribute("userPassword", "secret")
        attributes.add(attrUserPassword)

        # Update givenName attribute
        for attribute in attributes:
            attrName = attribute.getName()
            if ("givenname" == StringHelper.toLowerCase(attrName)):
                attribute.setValue(StringHelper.removeMultipleSpaces(attribute.getValue()) + " (updated)")

        return True
