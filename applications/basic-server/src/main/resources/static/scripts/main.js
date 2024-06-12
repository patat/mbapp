'use strict';
(function() {
    const state = {};
    document.addEventListener('DOMContentLoaded', function () {
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
        fetchNextRound({
            battleId: state.battleId,
            roundId: data.roundId,
            winnerId: data.winnerId,
        }).then(round => {
            console.log('round', round);
            state.roundId = round.id;

            if (!round.movie1Id && !round.movie2Id) {
                const winner = state.movies.find(movie => movie.id === round.winnerId);
                return render(Movie(winner))
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

        const showcaseContainerCover = document.createElement('div');
        showcaseContainerCover.classList.add('showcase-container__cover');

        const fightBtn = document.createElement('button');
        fightBtn.textContent = 'FIGHT!'
        fightBtn.addEventListener('click', onFightBtnClick);

        showcaseContainerCover.append(fightBtn);
        showcaseContainer.append(...movies.map(MoviePoster), showcaseContainerCover);

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

        roundContainer.append(
            Movie(movie1),
            Movie(movie2)
        );

        return roundContainer;
    }

    function Movie(movie) {
        const movieCard = document.createElement('div');
        movieCard.classList.add('movie-card');

        const poster = MoviePoster(movie);
        const title = document.createElement('div');
        title.textContent = movie.title;

        movieCard.append(poster, title);

        movieCard.addEventListener('click', () => {
            renderNextRound({
                roundId: state.roundId,
                winnerId: movie.id,
            });
        });

        return movieCard;
    }

    function render(content) {
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

