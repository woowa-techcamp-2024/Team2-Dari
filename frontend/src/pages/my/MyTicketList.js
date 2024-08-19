import {useState} from "react"
import {Button} from "../../components/ui/button"
import {Link} from 'react-router-dom';

export default function MyTicketList() {
    const [selectedTicket, setSelectedTicket] = useState(null)
    const tickets = [
        {
            name: "Early Bird Ticket",
            price: 25000,
            description: "Get your ticket early and save!",
            festival: {
                name: "Coachella Music Festival",
                image: "/placeholder.svg",
                date: "April 14-16, 2023",
                location: "Indio, California",
            },
        },
        {
            name: "Regular Ticket",
            price: 30000,
            description: "Standard ticket price",
            festival: {
                name: "Lollapalooza",
                image: "/placeholder.svg",
                date: "July 28-31, 2023",
                location: "Chicago, Illinois",
            },
        },
        {
            name: "VIP Ticket",
            price: 50000,
            description: "Exclusive access and perks",
            festival: {
                name: "Bonnaroo Music & Arts Festival",
                image: "/placeholder.svg",
                date: "June 15-18, 2023",
                location: "Manchester, Tennessee",
            },
        },
    ]
    return (
        <div className="flex flex-col items-center w-full">
            <header className="w-full p-4 bg-white border-b">
                <div className="flex items-center justify-between">
                    <div className="flex items-center">
                        <LogInIcon className="w-8 h-8"/>
                        <span className="ml-2 text-xl font-bold">festa</span>
                    </div>
                    <Button variant="outline" className="flex items-center">
                        <MenuIcon className="w-6 h-6"/>
                        <span className="sr-only">Menu</span>
                    </Button>
                </div>
            </header>
            <main className="w-full p-4 space-y-8">
                <section>
                    <div className="bg-white rounded-lg shadow-lg p-6">
                        <h2 className="text-2xl font-bold mb-4">My Tickets</h2>
                        {tickets.map((ticket, index) => (
                            <div key={index} className="bg-muted rounded-lg p-4 mb-4">
                                <div className="flex items-center mb-4">
                                    <img
                                        src="/placeholder.svg"
                                        alt={ticket.festival.name}
                                        width={100}
                                        height={100}
                                        className="rounded-lg mr-4"
                                        style={{aspectRatio: "100/100", objectFit: "cover"}}
                                    />
                                    <div>
                                        <h3 className="text-lg font-bold">{ticket.festival.name}</h3>
                                        <p className="text-sm text-muted-foreground">
                                            {ticket.festival.date} - {ticket.festival.location}
                                        </p>
                                    </div>
                                </div>
                                <div className="flex items-center justify-between">
                                    <div>
                                        <h4 className="text-lg font-bold">{ticket.name}</h4>
                                        <p className="text-sm text-muted-foreground">{ticket.description}</p>
                                    </div>
                                    <div className="text-2xl font-bold">{ticket.price.toLocaleString()}원</div>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>
            </main>
            <footer className="w-full p-4 bg-white border-t">
                <div className="flex flex-col items-center space-y-4">
                    <div className="flex items-center">
                        <LogInIcon className="w-8 h-8"/>
                        <span className="ml-2 text-xl font-bold">festa</span>
                    </div>
                    <div className="flex flex-col items-center space-y-2">
                        <Link href="#" className="text-sm text-muted-foreground" prefetch={false}>
                            소개
                        </Link>
                        <Link href="#" className="text-sm text-muted-foreground" prefetch={false}>
                            이벤트
                        </Link>
                        <Link href="#" className="text-sm text-muted-foreground" prefetch={false}>
                            고객센터
                        </Link>
                    </div>
                    <div className="text-xs text-muted-foreground">© 2024 Festa, Inc. All rights reserved.</div>
                </div>
            </footer>
        </div>
    )
}

function LogInIcon(props) {
    return (
        <svg
            {...props}
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
        >
            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
            <polyline points="10 17 15 12 10 7"/>
            <line x1="15" x2="3" y1="12" y2="12"/>
        </svg>
    )
}


function MenuIcon(props) {
    return (
        <svg
            {...props}
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
        >
            <line x1="4" x2="20" y1="12" y2="12"/>
            <line x1="4" x2="20" y1="6" y2="6"/>
            <line x1="4" x2="20" y1="18" y2="18"/>
        </svg>
    )
}