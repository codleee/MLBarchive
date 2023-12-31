"use client";
import { useSelector } from "react-redux";
import { useRouter } from "next/navigation";
import { wrap } from "module";

const FollowedPlayer = () => {
  const followList = useSelector((state: any) => state.user.followList);
  const router = useRouter();
  return (
    <>
      <div className="main_title">[ FOLLOW PLAYER ]</div>
      <div>
        {followList?.PlayerList ? (
          <div>
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                flexWrap: "wrap",
              }}
            >
              {followList.PlayerList.map((player: any) => (
                <div
                  key={player.playerId}
                  onClick={() => router.push(`/players/${player.playerId}`)}
                >
                  <div className="miniCard4">
                    <img
                      src={player.image}
                      alt="이미지없음"
                      className="playerImage6"
                    />
                    <div>{player.name}</div>
                    <div>{player.korName}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <p>팔로우한 선수들이 없습니다.</p>
        )}
      </div>
    </>
  );
};

export default FollowedPlayer;
