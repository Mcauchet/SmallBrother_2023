<#import "_layout.ftl" as layout />
<@layout.header>
    <div class="column">
        <p>
            <!--ajouter texte sur l'app, les liens, etc.-->
        </p>
    </div>
    <div class="column">
        <form action="/login" method="post">
            <label for="email">Email:</label>
            <input type="text" id="email" name="email">
            <br>
            <label for="password">Password:</label>
            <input type="password" id="password" name="password">
            <br>
            <input type="submit" value="Login">
        </form>
    </div>
</@layout.header>
