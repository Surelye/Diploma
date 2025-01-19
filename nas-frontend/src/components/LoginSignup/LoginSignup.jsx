import React, {useState} from 'react'
import './LoginSignup.css'

import user_icon from '../assets/person.png'
import password_icon from '../assets/password.png'
import axiosClient from "../../api/axios.config.js";
import {useNavigate} from "react-router-dom";

const LoginSignup = () => {
    const [action, setAction] = useState("Sign Up");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (event) => {
        event.preventDefault();

        const url = action === "Sign Up" ? "/api/users/register" : "/api/auth/login";

        try {
            const response = await axiosClient.post(url, {
                username: username,
                password: password
            });

            if (response.data) {
                localStorage.setItem("username", username);
                localStorage.setItem("password", password);
                navigate("/explore")
            }

        } catch (error) {
            console.log(error);
        }
    }

    return (
        <div className="container">
            <div className="header">
                <div className="text">{action}</div>
                <div className="underline"></div>
            </div>
            <form className="inputs" onSubmit={handleSubmit}>
                <div className="input">
                    <img src={user_icon} alt="user icon"/>
                    <input type="text" placeholder="Username" value={username} required
                           onChange={(e) => setUsername(e.target.value)}/>
                </div>
                <div className="input">
                    <img src={password_icon} alt="password icon"/>
                    <input type="password" placeholder="Password" value={password} required
                           onChange={(e) => setPassword(e.target.value)}/>
                </div>
                <button type="submit" className="submit"
                        style={{marginLeft: '10%', display: 'block', width: '80%'}}>Submit
                </button>
            </form>
            <div className="submit-container">
                <div className={action === "Login" ? "submit gray" : "submit"}
                     onClick={() => setAction("Sign Up")}>Sign Up
                </div>
                <div className={action === "Sign Up" ? "submit gray" : "submit"}
                     onClick={() => setAction("Login")}>Login
                </div>
            </div>
            {
                action === "Sign Up"
                    ? <div/>
                    : <div className="forgot-password">
                        Forgot password?
                        <span> Click here!</span>
                    </div>
            }
        </div>
    )
}

export default LoginSignup;