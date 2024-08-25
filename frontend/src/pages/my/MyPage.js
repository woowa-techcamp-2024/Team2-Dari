import React, { useState, useEffect } from 'react';
import { Tab } from '@headlessui/react';
import apiClient from '../../utils/apiClient';
import HostedFestivals from './HostedFestival';
import PurchasedTickets from './PurchasedTickets';

function classNames(...classes) {
  return classes.filter(Boolean).join(' ')
}

export default function MyPage() {
  const [selectedIndex, setSelectedIndex] = useState(0);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <h1 className="text-3xl font-bold text-teal-600 mb-6">마이페이지</h1>
      <Tab.Group selectedIndex={selectedIndex} onChange={setSelectedIndex}>
        <Tab.List className="flex space-x-1 rounded-xl bg-teal-900/20 p-1">
          <Tab
            className={({ selected }) =>
              classNames(
                'w-full rounded-lg py-2.5 text-sm font-medium leading-5 text-teal-700',
                'ring-white ring-opacity-60 ring-offset-2 ring-offset-teal-400 focus:outline-none focus:ring-2',
                selected
                  ? 'bg-white shadow'
                  : 'text-teal-100 hover:bg-white/[0.12] hover:text-white'
              )
            }
          >
            주최한 축제
          </Tab>
          <Tab
            className={({ selected }) =>
              classNames(
                'w-full rounded-lg py-2.5 text-sm font-medium leading-5 text-teal-700',
                'ring-white ring-opacity-60 ring-offset-2 ring-offset-teal-400 focus:outline-none focus:ring-2',
                selected
                  ? 'bg-white shadow'
                  : 'text-teal-100 hover:bg-white/[0.12] hover:text-white'
              )
            }
          >
            구매한 티켓
          </Tab>
        </Tab.List>
        <Tab.Panels className="mt-2">
          <Tab.Panel>
            <HostedFestivals />
          </Tab.Panel>
          <Tab.Panel>
            <PurchasedTickets />
          </Tab.Panel>
        </Tab.Panels>
      </Tab.Group>
    </div>
  );
}