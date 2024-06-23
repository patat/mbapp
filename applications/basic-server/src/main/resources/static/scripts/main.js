'use strict';
(function() {
    const state = {};
    document.addEventListener('DOMContentLoaded', function () {
        state.body = document.querySelector('body');
        state.root = selectElement('app-root');
        const newBattleBtn = selectElement('new-battle-btn');
        console.log('newBattleBtn', newBattleBtn);

        newBattleBtn.addEventListener('click', onNewBattleBtnClick);
    });

    function onNewBattleBtnClick() {
        fetch('/showcase-movies')
            .then(response => {
                response.json().then(data => {
                    console.log('movies', data);
                    state.movies = data.movies;
                    state.battleId = data.battleId;

                    render(ShowcaseMovies(data.movies));
                })
            })
            .catch(reason => console.error(reason));
    }

    function onFightBtnClick() {
        renderNextRound({
            roundId: null,
            winnerId: null,
        });
    }

    function renderNextRound(data) {
        state.roundCnt = state.roundCnt ? state.roundCnt + 1 : 0;

        fetchNextRound({
            battleId: state.battleId,
            roundId: data.roundId,
            winnerId: data.winnerId,
        }).then(round => {
            console.log('round', round);
            state.roundId = round.id;

            if (!round.movie1Id && !round.movie2Id) {
                const winner = state.movies.find(movie => movie.id === round.winnerId);
                return render(Winner(winner), 'dark');
            }

            const movie1 = state.movies.find(movie => movie.id === round.movie1Id);
            const movie2 = state.movies.find(movie => movie.id === round.movie2Id);

            render(Round(movie1, movie2));
        });
    }

    function fetchNextRound(data) {
        return fetch('/next-round', {
            method: "POST",
            mode: "same-origin",
            cache: "no-cache",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
        }).then(response => {
            return response.json().catch(reason => console.error(reason));
        }).catch(reason => console.error(reason));
    }

    function ShowcaseMovies(movies) {
        const showcaseContainer = document.createElement('div');
        showcaseContainer.classList.add('showcase-container');

        const showcaseContainerBtn = document.createElement('div');
        showcaseContainerBtn.classList.add('showcase-container__btn');

        const fightBtn = document.createElement('button');
        fightBtn.classList.add('sec-btn');
        fightBtn.textContent = 'FIGHT !';
        fightBtn.addEventListener('click', onFightBtnClick);

        showcaseContainerBtn.append(fightBtn);
        showcaseContainer.append(...movies.map(MoviePoster), showcaseContainerBtn);

        return showcaseContainer;
    }

    function MoviePoster(movie) {
        const poster = document.createElement('div');
        poster.classList.add('movie-poster');
        poster.style.backgroundImage = `url(https://media.themoviedb.org/t/p/w220_and_h330_face${movie.posterPath})`;
        return poster
    }

    function Round(movie1, movie2) {
        const roundContainer = document.createElement('div');
        roundContainer.classList.add('round-container');

        const vs = document.createElement('div');
        vs.classList.add('vs');
        vs.textContent = 'VS';

        roundContainer.append(
            Movie(movie1),
            vs,
            Movie(movie2)
        );

        return roundContainer;
    }

    function Movie(movie, reverse) {
        const movieCard = document.createElement('div');
        movieCard.classList.add('movie-card');

        if (reverse) {
            movieCard.classList.add('movie-card--reverse');
        }

        const title = document.createElement('div');
        title.classList.add('movie-card__title');
        title.textContent = movie.title;

        const fightBar = document.createElement('div');
        fightBar.classList.add('movie-card__fight-bar');

        const poster = MoviePoster(movie);
        const overview = document.createElement('p');
        overview.classList.add('movie-card__overview');
        overview.textContent = movie.overview;

        const info = document.createElement('div');
        info.classList.add('movie-card__info');

        info.append(poster, overview);

        movieCard.append(title, fightBar, info);

        movieCard.addEventListener('click', () => {
            renderNextRound({
                roundId: state.roundId,
                winnerId: movie.id,
            });
        });

        return movieCard;
    }

    function Winner(movie) {
        const winnerContainer = document.createElement('div');
        winnerContainer.classList.add('winner');

        const winnerText = document.createElement('img');
        winnerText.src = '/static/images/winner.svg';
        winnerText.classList.add('winner__text');

        const poster = MoviePoster(movie);

        winnerContainer.append(
            winnerText,
            poster
        );

        return winnerContainer;
    }

    function render(content, theme) {
        if (theme === 'dark' && !state.body.classList.contains('theme-dark')) {
            state.body.classList.add('theme-dark');
        } else if (theme !== 'dark' && state.body.classList.contains('theme-dark')) {
            state.body.classList.remove('theme-dark');
        }

        if (Array.isArray(content)) {
            state.root.replaceChildren(...content);

            return;
        }

        state.root.replaceChildren(content);
    }

    function selectElement(dataJs) {
        return document.querySelector(`[data-js='${dataJs}']`);
    }

    function selectElements(dataJs) {
        return document.querySelectorAll(`[data-js='${dataJs}']`);
    }
})();

