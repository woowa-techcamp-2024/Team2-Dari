import React from 'react';
import { Link } from 'react-router-dom';

const Layout = ({ children }) => {
    return (
        <div className="flex flex-col min-h-screen">
            <header className="bg-white shadow-md">
                <nav className="container mx-auto px-4 py-6">
                    <div className="flex justify-between items-center">
                        <Link to="/" className="text-2xl font-bold text-primary">Festa</Link>
                        <ul className="flex space-x-4">
                            <li><Link to="/" className="hover:text-primary">Home</Link></li>
                            <li><Link to="/about" className="hover:text-primary">About</Link></li>
                            <li><Link to="/contact" className="hover:text-primary">Contact</Link></li>
                        </ul>
                    </div>
                </nav>
            </header>
            <main className="flex-grow container mx-auto px-4 py-8">
                {children}
            </main>
            <footer className="bg-gray-200">
                <div className="container mx-auto px-4 py-6 text-center">
                    <p>&copy; 2024 Festa. All rights reserved.</p>
                </div>
            </footer>
        </div>
    );
};

export default Layout;