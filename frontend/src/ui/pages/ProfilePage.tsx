import {
    useNavigate
} from "react-router-dom";

import {
    useAuth
} from "../../contexts/AuthContext";

import "../styles/profilePage.css";


export default function ProfilePage() {

    const navigate =
        useNavigate();


    const {
        user
    } = useAuth();


    if (!user) {
        return null;
    }


    return (

        <div className="profile-page">

            <header className="profile-header">

                <button
                    onClick={() =>
                        navigate("/chats")
                    }
                >
                    ← Back
                </button>


                <h2>
                    Profile
                </h2>

            </header>


            <main className="profile-content">

                <div className="profile-field">

                    <span className="profile-label">
                        Username
                    </span>

                    <span className="profile-value">
                        {user.username}
                    </span>

                </div>


                <div className="profile-field">

                    <span className="profile-label">
                        Handle
                    </span>

                    <span className="profile-value">
                        @{user.handle}
                    </span>

                </div>

            </main>

        </div>

    );

}