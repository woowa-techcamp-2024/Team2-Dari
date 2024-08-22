import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from './AuthContext';

const Header = () => {
    const { isAuthenticated, logout } = useAuth();

    return (
        <header className="bg-background shadow-sm">
            <div className="container mx-auto px-4 py-4 flex justify-between items-center">
                <div className="flex items-center">
                    <Link to="/" className="text-2xl font-bold text-primary">축제의 민족</Link>
                </div>
                <nav>
                    <ul className="flex space-x-4">
                        <li><Link to="/about" className="text-foreground hover:text-primary transition-colors">About</Link></li>
                        <li><Link to="/contact" className="text-foreground hover:text-primary transition-colors">Contact</Link></li>
                        {isAuthenticated ? (
                            <li>
                                <button 
                                    onClick={logout} 
                                    className="text-foreground hover:text-primary transition-colors"
                                >
                                    Logout
                                </button>
                            </li>
                        ) : (
                            <li><Link to="/login" className="text-foreground hover:text-primary transition-colors">Login</Link></li>
                        )}
                    </ul>
                </nav>
            </div>
        </header>
    );
};

export default Header;
