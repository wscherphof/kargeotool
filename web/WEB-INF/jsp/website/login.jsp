<%@include file="/WEB-INF/taglibs.jsp" %>

<div id="leftspacecontent">    
    <div id="tekst">        
        <div class="loginBox">
            <div class="loginboxTitel">Log in</div>
            <div>
                <tiles:insert definition="infoblock"/>
                <form id="loginForm" action="j_security_check" method="POST">
                    <table>
                        <tr><td>Gebruikersnaam:</td><td><input type="text" name="j_username" size="36"></td></tr>
                        <tr><td>Wachtwoord:</td><td><input type="password" name="j_password" size="36"></td></tr>
                        <tr><td style="text-align: right;" colspan="2"><input type="Submit" value="Login"></td></tr>
                    </table>
                </form>
            </div>
        </div>
        <script language="JavaScript">
            <!--
            document.forms.loginForm.j_username.focus();
            // -->
        </script>
    </div>
</div>
