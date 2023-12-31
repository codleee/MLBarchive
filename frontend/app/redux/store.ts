import { configureStore } from "@reduxjs/toolkit";
import createSagaMiddleware from "@redux-saga/core";
import rootSaga from "@/app/redux/services/rootSaga";
import userReducer from "./features/userSlice"
import searchTeamReducer from "./features/searchTeamSlice"
import playerDetailReducer from "./features/playerDetailSlice"
import searchPlayerReducer from "./features/searchPlayerSlice"
import matchReducer from "./features/matchSlice"
import teamReduce from "./features/teamSlice"

const sagaMiddleware = createSagaMiddleware()
const sessionSaverMiddleware = (store: any) => (next: any) => (action: any) => {
  let result = next(action);
  if (typeof window !== 'undefined') {
    const userState = store.getState().user.refreshToken;
    if (userState) {
      sessionStorage.setItem('refreshToken', JSON.stringify(userState));
    }
  }
  return result;
};

export const store = configureStore({
  reducer: {
    user: userReducer,
    searchTeam: searchTeamReducer,
    playerDetail: playerDetailReducer,
    searchPlayer: searchPlayerReducer,
    match: matchReducer,
    team: teamReduce,
  },
  devTools: process.env.NODE_ENV !== "production",
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(sagaMiddleware)
      .concat(sessionSaverMiddleware)
})

sagaMiddleware.run(rootSaga)

export type RootState = ReturnType<typeof store.getState>

export type AppDispatch = typeof store.dispatch
// export const persistor = persistStore(store)
