import {Button} from "../../components/ui/button"
import {Input} from "../../components/ui/input"
import {Card} from "../../components/ui/card"
import {Link} from 'react-router-dom';

export default function Component() {
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
                <div className="mt-4">
                    <Input
                        type="search"
                        placeholder="어떤 이벤트를 찾고 있나요?"
                        className="w-full px-4 py-2 border rounded-full"
                    />
                </div>
            </header>
            <main className="w-full p-4 space-y-8">
                <section>
                    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                        <Card className="flex flex-col">
                            <Link to="#" className="relative group" prefetch={false}>
                                <img
                                    src="/placeholder.svg"
                                    alt="Event image"
                                    className="w-full rounded-t-lg object-cover aspect-[4/3]"
                                    width="300"
                                    height="200"
                                />
                                <div
                                    className="absolute inset-0 bg-black/50 rounded-t-lg opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                    <Button variant="outline" size="sm">
                                        View Details
                                    </Button>
                                </div>
                            </Link>
                            <div className="p-4 flex-1 flex flex-col justify-between">
                                <div>
                                    <h3 className="text-lg font-bold">2030 미래전망</h3>
                                    <p className="text-sm text-muted-foreground">2023.09.30</p>
                                    <p className="mt-2 text-sm">2030년 미래를 전망하는 특별한 이벤트에 초대합니다.</p>
                                </div>
                                <div className="mt-4">
                                    <div className="flex items-center justify-between">
                                        <div className="text-sm text-muted-foreground">Hosted by: Festa</div>
                                        <Button variant="outline" size="sm">
                                            Buy Ticket
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </Card>
                        <Card className="flex flex-col">
                            <Link to="#" className="relative group" prefetch={false}>
                                <img
                                    src="/placeholder.svg"
                                    alt="Event image"
                                    className="w-full rounded-t-lg object-cover aspect-[4/3]"
                                    width="300"
                                    height="200"
                                />
                                <div
                                    className="absolute inset-0 bg-black/50 rounded-t-lg opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                    <Button variant="outline" size="sm">
                                        View Details
                                    </Button>
                                </div>
                            </Link>
                            <div className="p-4 flex-1 flex flex-col justify-between">
                                <div>
                                    <h3 className="text-lg font-bold">Charming Switch 개그 콘서트</h3>
                                    <p className="text-sm text-muted-foreground">2023.09.30</p>
                                    <p className="mt-2 text-sm">Charming Switch와 함께하는 특별한 개그 콘서트!</p>
                                </div>
                                <div className="mt-4">
                                    <div className="flex items-center justify-between">
                                        <div className="text-sm text-muted-foreground">Hosted by: Festa</div>
                                        <Button variant="outline" size="sm">
                                            Buy Ticket
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </Card>
                        <Card className="flex flex-col">
                            <Link to="#" className="relative group" prefetch={false}>
                                <img
                                    src="/placeholder.svg"
                                    alt="Event image"
                                    className="w-full rounded-t-lg object-cover aspect-[4/3]"
                                    width="300"
                                    height="200"
                                />
                                <div
                                    className="absolute inset-0 bg-black/50 rounded-t-lg opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                    <Button variant="outline" size="sm">
                                        View Details
                                    </Button>
                                </div>
                            </Link>
                            <div className="p-4 flex-1 flex flex-col justify-between">
                                <div>
                                    <h3 className="text-lg font-bold">사업계획서 필승 바이블</h3>
                                    <p className="text-sm text-muted-foreground">2023.09.30</p>
                                    <p className="mt-2 text-sm">성공적인 사업계획서를 작성하는 방법을 알려드립니다.</p>
                                </div>
                                <div className="mt-4">
                                    <div className="flex items-center justify-between">
                                        <div className="text-sm text-muted-foreground">Hosted by: Festa</div>
                                        <Button variant="outline" size="sm">
                                            Buy Ticket
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </Card>
                        <Card className="flex flex-col">
                            <Link to="#" className="relative group" prefetch={false}>
                                <img
                                    src="/placeholder.svg"
                                    alt="Event image"
                                    className="w-full rounded-t-lg object-cover aspect-[4/3]"
                                    width="300"
                                    height="200"
                                />
                                <div
                                    className="absolute inset-0 bg-black/50 rounded-t-lg opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                    <Button variant="outline" size="sm">
                                        View Details
                                    </Button>
                                </div>
                            </Link>
                            <div className="p-4 flex-1 flex flex-col justify-between">
                                <div>
                                    <h3 className="text-lg font-bold">GDSC DGU DEMO DAY</h3>
                                    <p className="text-sm text-muted-foreground">2023.09.30</p>
                                    <p className="mt-2 text-sm">GDSC DGU의 데모 데이 이벤트에 참여하세요!</p>
                                </div>
                                <div className="mt-4">
                                    <div className="flex items-center justify-between">
                                        <div className="text-sm text-muted-foreground">Hosted by: Festa</div>
                                        <Button variant="outline" size="sm">
                                            Buy Ticket
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </Card>
                        <Card className="flex flex-col">
                            <Link to="#" className="relative group" prefetch={false}>
                                <img
                                    src="/placeholder.svg"
                                    alt="Event image"
                                    className="w-full rounded-t-lg object-cover aspect-[4/3]"
                                    width="300"
                                    height="200"
                                />
                                <div
                                    className="absolute inset-0 bg-black/50 rounded-t-lg opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                    <Button variant="outline" size="sm">
                                        View Details
                                    </Button>
                                </div>
                            </Link>
                            <div className="p-4 flex-1 flex flex-col justify-between">
                                <div>
                                    <h3 className="text-lg font-bold">2030 미래전망</h3>
                                    <p className="text-sm text-muted-foreground">2023.09.30</p>
                                    <p className="mt-2 text-sm">2030년 미래를 전망하는 특별한 이벤트에 초대합니다.</p>
                                </div>
                                <div className="mt-4">
                                    <div className="flex items-center justify-between">
                                        <div className="text-sm text-muted-foreground">Hosted by: Festa</div>
                                        <Button variant="outline" size="sm">
                                            Buy Ticket
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </Card>
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
                        <Link to="#" className="text-sm text-muted-foreground" prefetch={false}>
                            소개
                        </Link>
                        <Link to="#" className="text-sm text-muted-foreground" prefetch={false}>
                            이벤트
                        </Link>
                        <Link to="#" className="text-sm text-muted-foreground" prefetch={false}>
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