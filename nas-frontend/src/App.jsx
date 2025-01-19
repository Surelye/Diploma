import {Navigate, Route, Routes} from "react-router-dom";
import LoginSignup from "./components/LoginSignup/LoginSignup.jsx";
import FileExplorer from "./components/FileExplorer/FileExplorer.jsx";

function App() {

    return (
        <>
            <Routes>
                <Route path={"/"} element={<Navigate to={"/login"}/>}/>
                <Route path={"/login"} element={<LoginSignup />} />
                <Route path={"/explore/*"} element={<FileExplorer />} />
            </Routes>
        </>
    )
}

export default App
