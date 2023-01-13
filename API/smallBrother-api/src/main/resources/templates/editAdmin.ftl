<#import "_layout.ftl" as layout />
<@layout.header>
    <form action="/admin/editAdmin" method="post">
        <label for="email">Email:</label>
        <input type="text" id="email" name="email">
        <br>
        <label for="previousPassword">Previous Password:</label>
        <input type="password" id="previousPassword" name="previousPassword">
        <br>
        <label for="newPassword">New Password:</label>
        <input type="password" id="newPassword" name="newPassword">
        <br>
        <label for="confirmPassword">Confirm New Password:</label>
        <input type="password" id="confirmPassword" name="confirmPassword">
        <br>
        <label for="phone">Phone:</label>
        <input type="text" id="phone" name="phone">
        <br>
        <br>
        <input type="submit" value="Edit">
    </form>
</@layout.header>