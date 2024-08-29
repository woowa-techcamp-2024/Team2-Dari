import { atom } from 'recoil';

export const waitOrdersState = atom({
    key: 'waitOrdersState',
    default: {} //key: ticketId, value: waitOrder
});