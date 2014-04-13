ImixsCrypt
==========

ImixsCrypt provides you an open, secure and transparent API to your privacy.

Your privacy
============

We think that your privacy is one of the most valuable things when your are using the Internet. 
ImixsCrypt gives you back the control of your privacy.

Don't trust
============

You should not trust any servers in the Internet which you have no control about. ImixsCrypt gives you a private local webserver. ImixsCrypt only runs on your local machine which you should trust in.

Keep your secrets
============

There is no reason to trust anyone in the Internet your secrets or your private password. Don't believe that this is necessary. With ImixsCrypt there is an easy solution to safely navigate the Internet.

Use Cryptography
============
Cryptography is nothing new. It exitss before the internet becomes part of your live. Imixs crypt uses a well known and trustable crypthography alogrithm called RSA. Read was RSA is and how it works.



Reflections of a redesign....
============

The current JAX-RS Implementation should be changed. Instead we can use node.js to provide a local server. Secondary we think using a existing OpenSSL infrastructure from the clients OS is more easy to use and more secure.

To Authenticate via a local web server we can use the "Public Key Authentication Method" from SSH (https://tools.ietf.org/html/rfc4252#section-7)

   With this method, the possession of a private key serves as
   authentication.  This method works by sending a signature created
   with a private key of the user.  The server MUST check that the key
   is a valid authenticator for the user, and MUST check that the
   signature is valid.  If both hold, the authentication request MUST be
   accepted; otherwise, it MUST be rejected.  Note that the server MAY
   require additional authentications after successful authentication.
   
   
SSH Framework for node.js
====
see: https://github.com/Medium/ursa
