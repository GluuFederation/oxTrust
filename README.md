oxTrust
======
oxTrust is a Weld based web application for Gluu Server administration.
  
oxTrust enables administrators to manage what information about people is being exposed to partner websites. oxTrust is also the local management interface that handles other server instance specific configurations, and provides a mechanism for IT administrators to support people at the organization who are having trouble accessing a website or network resource.

**oxTrust is tightly coupled with [oxAuth](https://github.com/GluuFederation/oxAuth).** 

oxAuth configuration is stored in LDAP, and it would be hard to generate the right configuration entries without oxTrust. The projects are left separate because in a high throughput cluster deployment, many oxAuth servers may be needed versus just a few oxTrust instances.

Refer to [https://github.com/GluuFederation/install](https://github.com/GluuFederation/install) for installation instructions.

To access Gluu support, please register and open a ticket on [Gluu Support](https://support.gluu.org).
