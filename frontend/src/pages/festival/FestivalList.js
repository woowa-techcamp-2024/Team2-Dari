import {Button} from "../../components/ui/button"
import {Input} from "../../components/ui/input"
import {Card} from "../../components/ui/card"
import {Link} from 'react-router-dom';

export default function FestivalList() {
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
                    <div className="grid gap-6 md:grid-cols-1">
                        <Card className="flex flex-col">
                            <div className="relative">
                                <img
                                    src="/placeholder.svg"
                                    alt="Event image"
                                    className="w-full rounded-t-lg object-cover aspect-[4/2]"
                                    width="800"
                                    height="400"
                                />
                                <div
                                    className="absolute inset-0 bg-black/50 rounded-t-lg flex items-center justify-center">
                                    <h1 className="text-3xl font-bold text-white">2030 미래전망</h1>
                                </div>
                            </div>
                            <div className="p-6 flex-1 flex flex-col justify-between">
                                <div>
                                    <p className="text-sm text-muted-foreground">2023.09.30</p>
                                    <p className="mt-4 text-lg">2030년 미래를 전망하는 특별한 이벤트에 초대합니다.</p>
                                    <p className="mt-4 text-sm">
                                        이 이벤트에서는 2030년의 미래를 다양한 관점에서 전망해볼 예정입니다. 기술, 경제, 사회, 문화 등
                                        다양한 분야의 전문가들이 참여하여 우리가 직면할 수 있는 변화와 기회에 대해 이야기할 것입니다.
                                    </p>
                                </div>
                                <div className="mt-6">
                                    <div className="flex items-center justify-between">
                                        <div className="text-sm text-muted-foreground">Hosted by: Festa</div>
                                        <Button size="sm">Buy Ticket</Button>
                                    </div>
                                </div>
                            </div>
                        </Card>
                    </div>
                </section>
                <section>
                    <h2 className="text-2xl font-bold mb-4">행사 정보</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                        <div>
                            <h3 className="text-lg font-bold mb-2">일시</h3>
                            <p className="text-sm text-muted-foreground">2023년 9월 30일 오후 2시</p>
                        </div>
                        <div>
                            <h3 className="text-lg font-bold mb-2">장소</h3>
                            <p className="text-sm text-muted-foreground">서울특별시 강남구 테헤란로 427</p>
                        </div>
                        <div>
                            <h3 className="text-lg font-bold mb-2">주최</h3>
                            <p className="text-sm text-muted-foreground">Festa</p>
                        </div>
                        <div>
                            <h3 className="text-lg font-bold mb-2">참가비</h3>
                            <p className="text-sm text-muted-foreground">30,000원</p>
                        </div>
                    </div>
                </section>
                <section>
                    <h2 className="text-2xl font-bold mb-4">행사 소개</h2>
                    <div className="text-sm text-muted-foreground">
                        <p>
                            2030년 미래를 전망하는 특별한 이벤트에 초대합니다. 이 이벤트에서는 기술, 경제, 사회, 문화 등 다양한 분야의
                            전문가들이 참여하여 우리가 직면할 수 있는 변화와 기회에 대해 이야기할 것입니다.
                        </p>
                        <p className="mt-4">
                            참가자 여러분께서는 이 이벤트를 통해 2030년의 미래를 다양한 관점에서 전망해볼 수 있을 것입니다. 또한
                            전문가들과의 토론을 통해 미래에 대한 통찰력을 얻을 수 있을 것입니다.
                        </p>
                        <p className="mt-4">
                            이 이벤트는 2023년 9월 30일 오후 2시에 서울특별시 강남구 테헤란로 427에서 개최될 예정입니다. 참가비는
                            30,000원이며, Festa에서 주최하는 행사입니다.
                        </p>
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