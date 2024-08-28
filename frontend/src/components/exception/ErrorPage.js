import React from 'react';
import {XCircleIcon} from '@heroicons/react/solid';

const ErrorPage = () => {
    return (

        <div className="flex flex-col items-center justify-center h-full px-4 py-16 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 text-center">
                <XCircleIcon className="mx-auto h-24 w-24 text-red-500"/>
                <h2 className="mt-6 text-3xl font-extrabold text-gray-900">
                    Oops! 문제가 발생했습니다
                </h2>
                <p className="mt-2 text-sm text-gray-600">
                    죄송합니다. 현재 서버에 일시적인 문제가 발생했습니다.
                </p>
                <div className="mt-8 space-y-4">
                    <button
                        onClick={() => window.location.reload()}
                        className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                    >
                        페이지 새로고침
                    </button>

                    <a href="/"
                       className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm
                        text-sm font-medium text-indigo-600 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2
                        focus:ring-offset-2 focus:ring-indigo-500"
                    >
                        홈페이지로 돌아가기
                    </a>
                </div>
            </div>
        </div>

    )
        ;
};

export default ErrorPage;