import React from 'react';

const FestivalInfo = ({ festival }) => (
  <div className="bg-white shadow-lg rounded-lg p-6">
    <h2 className="text-2xl font-bold mb-4">{festival.title}</h2>
    <img src={festival.festivalImg || "/placeholder.svg"} alt={festival.title} className="w-full h-64 object-cover rounded-lg mb-4" />
    <div className="grid grid-cols-2 gap-4 mb-4">
      <div>
        <p className="font-semibold">축제 ID:</p>
        <p>{festival.festivalId}</p>
      </div>
      <div>
        <p className="font-semibold">주최자 ID:</p>
        <p>{festival.adminId}</p>
      </div>
      <div>
        <p className="font-semibold">시작 시간:</p>
        <p>{new Date(festival.startTime).toLocaleString()}</p>
      </div>
      <div>
        <p className="font-semibold">종료 시간:</p>
        <p>{new Date(festival.endTime).toLocaleString()}</p>
      </div>
      <div>
        <p className="font-semibold">공개 상태:</p>
        <p>{festival.festivalPublicationStatus}</p>
      </div>
      <div>
        <p className="font-semibold">진행 상태:</p>
        <p>{festival.festivalProgressStatus}</p>
      </div>
    </div>
    <div>
      <p className="font-semibold">설명:</p>
      <p className="mt-2">{festival.description}</p>
    </div>
  </div>
);

export default FestivalInfo;