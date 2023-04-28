import React, {useState} from 'react';
import "./Login.css"
import Header from "../../components/Header/Header.js";

const Login = () => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");


    function handleLogin(e) {
        e.preventDefault();
        const user = {"username": username, "password": password};
        (user)
            .then(response => {
                sessionStorage.setItem('username', username);
                console.log("handle Login: " + response.data);
                window.location.replace("/");
            }).catch((error) => alert(error.response.data))
    }

    return (
        <>
            <Header searchBar={false} username={null}/>
            <div className="form-container">
                <h2>LOGIN</h2>
                <form action="" method="post" id="login-form">
                    <label>
                        <input className="form-input" name="username" type="text" placeholder="Username"
                               onChange={(event) => setUsername(event.target.value)} required/>
                    </label>
                    <label>
                        <input className="form-input" name="password" type="password" placeholder="Password"
                               onChange={(event) => setPassword(event.target.value)} required/>
                    </label>
                    <button type="submit" onClick={(e) => handleLogin(e)}>Register</button>
                </form>
            </div>
        </>
    );
}

export default Login;