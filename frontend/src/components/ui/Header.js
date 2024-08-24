import React from 'react';
import { Link } from 'react-router-dom';
import { Menu } from '@headlessui/react';
import { useAuth } from '../contexts/AuthContext';

const Header = () => {
    const { isAuthenticated, logout } = useAuth();

    return (
        <header className="bg-white shadow-sm">
            <div className="container mx-auto px-4 py-4 flex justify-between items-center">
                <Link to="/" className="flex items-center">
                    <img src="/logo.svg" alt="축제의 민족" className="h-8 mr-2" />
                    <span className="text-xl font-bold text-pink-500">축제의 민족</span>
                </Link>
                <nav>
                    <Menu as="div" className="relative">
                        <Menu.Button className="flex items-center space-x-2 text-gray-700 hover:text-pink-500">
                            <span>메뉴</span>
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" /></svg>
                        </Menu.Button>
                        <Menu.Items className="absolute right-0 w-56 mt-2 origin-top-right bg-white divide-y divide-gray-100 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
                            <div className="px-1 py-1">
                                <Menu.Item>
                                    {({ active }) => (
                                        <Link to="/" className={`${active ? 'bg-pink-500 text-white' : 'text-gray-900'} group flex rounded-md items-center w-full px-2 py-2 text-sm`}>
                                            Home
                                        </Link>
                                    )}
                                </Menu.Item>
                                <Menu.Item>
                                    {({ active }) => (
                                        <Link to="/about" className={`${active ? 'bg-pink-500 text-white' : 'text-gray-900'} group flex rounded-md items-center w-full px-2 py-2 text-sm`}>
                                            About
                                        </Link>
                                    )}
                                </Menu.Item>
                                {isAuthenticated ? (
                                    <>
                                        <Menu.Item>
                                            {({ active }) => (
                                                <Link to="/my/tickets" className={`${active ? 'bg-pink-500 text-white' : 'text-gray-900'} group flex rounded-md items-center w-full px-2 py-2 text-sm`}>
                                                    My Tickets
                                                </Link>
                                            )}
                                        </Menu.Item>
                                        <Menu.Item>
                                            {({ active }) => (
                                                <button onClick={logout} className={`${active ? 'bg-pink-500 text-white' : 'text-gray-900'} group flex rounded-md items-center w-full px-2 py-2 text-sm`}>
                                                    Logout
                                                </button>
                                            )}
                                        </Menu.Item>
                                    </>
                                ) : (
                                    <Menu.Item>
                                        {({ active }) => (
                                            <Link to="/login" className={`${active ? 'bg-pink-500 text-white' : 'text-gray-900'} group flex rounded-md items-center w-full px-2 py-2 text-sm`}>
                                                Login
                                            </Link>
                                        )}
                                    </Menu.Item>
                                )}
                            </div>
                        </Menu.Items>
                    </Menu>
                </nav>
            </div>
        </header>
    );
};

export default Header;