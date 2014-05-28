oxTrust
======
<p>oxTrust is a JBoss Seam web application for oxAuth administration, and for configuring inter-domain trust management. 
oxTrust enables administrators to manage what information about people is being exposed to which partner websites. oxTrust is also the local management interface that handles other server instance specific configurations, and provides a mechanism for IT administrators to support people at the organization who are having trouble accessing a website or network resource.</p>
<p>oxTrust is tightly coupled with <a href="https://github.com/GluuFederation/oxAuth">oxAuth</a>. oxAuth configuration is stored in LDAP, and it would be hard to generate the right configuration entries without oxTrust. The projects are separate projects because in a high throughput cluster deployment, many oxAuth servers are needed versus a few oxTrust instances.</p>
<p>Refer to <a href="https://github.com/GluuFederation/install">https://github.com/GluuFederation/install</a> for installation instructions.</p>
<p>To access Gluu support, please register and open a ticket on <a href="http://support.gluu.org" target="none">Gluu Support</a>
