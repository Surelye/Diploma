import React, {useEffect, useRef, useState} from 'react';
import './FileExplorer.css'
import axiosClient from "../../api/axios.config.js";
import {Link, useNavigate, useParams} from "react-router-dom";
import {Helmet} from "react-helmet";
import {defaultHeaders, getBasicAuthHeaderValue} from "../../api/AuthService.js";
import {resolvePath, resolveTypeEmoji} from "../../utils/ResolverUtils.js";

const FileExplorer = () => {
    const {'*': path} = useParams();
    const [items, setItems] = useState([]);
    const [selectedItems, setSelectedItems] = useState([]);
    const [allItemsSelected, setAllItemsSelected] = useState(false);
    const [filesToUpload, setFilesToUpload] = useState([]);
    const fileInputRef = useRef(null);
    const [action, setAction] = useState("Download");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [folderName, setFolderName] = useState("");
    const folderNameRef = useRef(null);
    const navigate = useNavigate();

    const toggleSelectAll = () => {
        if (allItemsSelected) {
            setSelectedItems([]);
        } else {
            const allItemNames = items.map(item => item.name);
            setSelectedItems(allItemNames);
        }
        setAllItemsSelected(!allItemsSelected);
    }

    const fetchData = async () => {
        try {
            const filesResponse = await axiosClient.get(`/api/files/list/${path}`, {
                headers: {"Authorization": getBasicAuthHeaderValue()},
            });
            setItems(filesResponse.data);
            console.log(filesResponse.data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleCheckboxChange = (itemName) => {
        if (selectedItems.includes(itemName)) {
            setSelectedItems(prevSelectedItems => prevSelectedItems.filter(name => name !== itemName))
            setSelectedItems(selectedItems.filter(name => name !== itemName));
        } else {
            setSelectedItems(prevSelectedItems => [...prevSelectedItems, itemName]);
        }
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (selectedItems.length === 0) {
            alert("Please select at least one item");
            return;
        }

        const url = action === "Download"
            ? `/api/files/download/${path}?filePaths=${selectedItems.join("&filePaths=")}`
            : `/api/files/delete/${path}`

        try {
            if (action === "Delete") {
                await axiosClient.delete(url, {
                    data: selectedItems,
                    headers: defaultHeaders
                });
                setSelectedItems([]);
                await fetchData();
            } else if (action === "Download") {
                const response = await axiosClient.get(url, {
                    headers: defaultHeaders,
                    responseType: "blob"
                })

                if (response.status === 200) {
                    const url = window.URL.createObjectURL(response.data);
                    const a = document.createElement('a'); // Create an anchor element
                    a.href = url;
                    a.download = 'download.zip';
                    document.body.appendChild(a);
                    a.click();
                    document.body.removeChild(a);
                    window.URL.revokeObjectURL(url);
                }
            }
        } catch (error) {
            console.log(error);
        }
    }

    const handleFileUpload = async () => {
        const formData = new FormData();

        for (let i = 0; i < filesToUpload.length; ++i) {
            formData.append("files", filesToUpload[i]);
        }

        try {
            const response = await axiosClient.post(`/api/files/upload/${path}`, formData, {
                headers: {
                    "Content-Type": "multipart/form-data",
                    "Authorization": getBasicAuthHeaderValue(),
                }
            });

            if (response.status === 201) {
                clearFileInput();
                await fetchData();
            }
        } catch (error) {
            console.log(error);
        }
    };

    const handleCreateFolder = async (event) => {
        event.preventDefault();

        if (folderName === '' || items.map(item => item.name).includes(folderName)) {
            return;
        }

        try {
            const response = await axiosClient.post(`/api/files/folder/${path}?folderName=${folderName}`, {}, {
                headers: {
                    "Authorization": getBasicAuthHeaderValue(),
                }
            });

            if (response.status === 201) {
                if (folderNameRef.current) {
                    folderNameRef.current.value = '';
                    if (!items.includes(folderName)) {
                        setItems(prevItems => [...prevItems, {
                            name: folderName,
                            creationTime: Date.now(),
                            lastModifiedTime: Date.now(),
                            size: 0,
                            directory: true,
                        }]);
                        setFolderName("");
                    }
                }
            }
        } catch (error) {
            console.log(error);
        }
    }

    const clearFileInput = () => {
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
            setFilesToUpload([]);
        }
    };

    const onBack = () => {
        navigate(-1);
    }

    const onLogout = () => {
        localStorage.removeItem("username");
        localStorage.removeItem("password");
        navigate("/login");
    }

    useEffect(() => {
        fetchData();
    }, [path]);

    if (loading) {
        return <div className="loading">Loading...</div>;
    }

    if (error) {
        return <div className="error">Error: {error}</div>;
    }

    return (
        <>
            <Helmet>
                <link rel="icon" type="image/svg+xml" href="../../../public/folder.svg"/>
                <title>Explorer</title>
            </Helmet>
            <div className="file-explorer">
                <h1>File Explorer</h1>

                <div className="arrow-container">
                    <button className="back-button" onClick={onBack}> ← Back</button>
                </div>

                <button className="logout-button" onClick={onLogout}>Logout</button>

                <ul>
                    {
                        items.length === 0
                            ? <div>
                                <div className="add-folder-button-container">
                                    <input type="text" placeholder="Enter folder name" className="folder-name-input"
                                           onChange={(e) => setFolderName(e.target.value)}
                                           value={folderName} required ref={folderNameRef}/>
                                    <button type="button" className="add-folder-button"
                                            onClick={handleCreateFolder}>Add folder
                                    </button>
                                    <div className="upload-container">
                                        <input type="file" className="browse-button" multiple ref={fileInputRef}
                                               onChange={(event) => setFilesToUpload(event.target.files)}
                                        />
                                        <button type="button" onClick={handleFileUpload}
                                                className="upload-arrow-button">
                                            ↑
                                        </button>
                                        <button type="button" onClick={clearFileInput} className="deselect-button">
                                            ✖
                                        </button>
                                    </div>
                                </div>
                                <li className="empty-message">
                                    No items available.
                                </li>
                            </div>
                            : <form onSubmit={handleSubmit}>
                                <section className="file-enumeration">
                                    <div className="file-enumeration-inner-container">
                                        <div>
                                            <button type="button" onClick={toggleSelectAll} className="select-all-button"
                                                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#0056b3'}
                                                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#007BFF'}
                                            >
                                                {allItemsSelected ? 'Deselect All' : 'Select All'}
                                            </button>
                                            <div className="add-folder-button-container">
                                                <input type="text" placeholder="Enter folder name"
                                                       className="folder-name-input"
                                                       onChange={(e) => setFolderName(e.target.value)}
                                                       value={folderName} ref={folderNameRef}/>
                                                <button type="button" className="add-folder-button"
                                                        onClick={handleCreateFolder}>Add folder
                                                </button>
                                            </div>
                                        </div>
                                        <div className="upload-container">
                                            <input type="file" className="browse-button" multiple ref={fileInputRef}
                                                   onChange={(event) => setFilesToUpload(event.target.files)}
                                            />
                                            <button type="button" onClick={handleFileUpload}
                                                    className="upload-arrow-button">
                                                ↑
                                            </button>
                                            <button type="button" onClick={clearFileInput} className="deselect-button">
                                                ✖
                                            </button>
                                        </div>
                                    </div>
                                    {items.map((item, index) => (
                                        <li key={index} className={item.directory ? 'directory' : 'file'}>
                                            <input type="checkbox" name="element" id={item.name} value={item.name}
                                                   onChange={() => handleCheckboxChange(item.name)}
                                                   checked={selectedItems.includes(item.name)}
                                            />
                                            <label htmlFor={item.name} className="item-name">
                                                {
                                                    item.directory
                                                        ? <Link to={`/explore/${resolvePath(path, item.name)}`}
                                                                style={{textDecoration: 'none'}}>
                                                            {resolveTypeEmoji(item)} {item.name}</Link>
                                                        : `${resolveTypeEmoji(item)} ${item.name}`
                                                }
                                            </label>
                                            <span className="item-details">
                                        Created at: {new Date(item.creationTime).toDateString()}
                                                {
                                                    item.directory
                                                        ? ` (Directory, ${item.size} bytes)`
                                                        : ` (Size: ${item.size} bytes)`
                                                }
                                    </span>
                                        </li>
                                    ))
                                    }</section>
                                <div style={{display: 'flex', justifyContent: 'space-between'}}>
                                    <button type="submit" className="submit"
                                            style={{marginTop: '25px', marginLeft: '10%', width: '30%'}}
                                            onClick={() => setAction("Download")}>Download
                                    </button>
                                    <button type="submit" className="submit"
                                            style={{marginTop: '25px', marginRight: '10%', width: '30%'}}
                                            onClick={() => setAction("Delete")}>Delete
                                    </button>
                                </div>
                            </form>
                    }
                </ul>
            </div>
        </>
    );
};

export default FileExplorer;