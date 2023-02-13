<#import "_layout.ftl" as layout />
<style>
    * {
        box-sizing: border-box;
    }
    .column {
        float: left;
        width: 50%;
        padding: 10px;
    }
    .footer {
        background-color: #f1f1f1;
        padding: 10px;
        text-align: center;
    }
    input {
        padding: 4px;
    }
    label {
        alignment: left;
    }
    @media (max-width: 600px) {
        .column {
            width: 100%;
        }
    }
</style>
<@layout.header>
    <div class="column">
            <!--ajouter texte sur l'app, les liens, etc.-->
        <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Aliquam aliquid aperiam aspernatur assumenda, esse
            eum inventore molestias natus neque placeat quaerat, quas totam voluptate? Consequatur nihil quis quisquam
            ratione tenetur.</p>
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
    <div class="footer">FOOTER</div>
</@layout.header>
