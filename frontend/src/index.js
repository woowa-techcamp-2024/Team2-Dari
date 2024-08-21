import React from 'react';
import {HashRouter} from 'react-router-dom';
import ReactDOM from 'react-dom/client';
import App from './App';
import './app.css';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <HashRouter>
        <App/>
    </HashRouter>,
);
