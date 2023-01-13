<#import "_layout.ftl" as layout />
<@layout.header>
    <form action="/login" method="post">
        <label for="email">Email:</label>
        <input type="text" id="email" name="email">
        <br>
        <label for="password">Password:</label>
        <input type="password" id="password" name="password">
        <br>
        <input type="submit" value="Login">
    </form>
</@layout.header>
