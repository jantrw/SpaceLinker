# 🌌 SpaceLinker

A SpaceLinker that connects people with space – with support for NASA APIs and ISS tracking.

## Features

- **ISS Tracking**: Determines the current position (ocean or land with country + city), speed, and altitude of the International Space Station (ISS).

  ![ISS-Output](images/ISS_Output.png)

- **NASA Astronomy Picture of the Day**: Displays the current NASA Picture of the Day.

  ![NASA-Picture-Output](images/NasaPictureOfTheDay_Output.png)

- **NASA Astronomy Picture of the Day with Info**: Displays the current NASA Picture of the Day with its description.

  ![NASA-Picture-Info-Output](images/NasaPictureOfTheDayInfo_Output.png)

- **Slash Commands**: Supports modern Discord slash commands.

  ![Slash_Command](images/Slash_Command.png)

## Installation

### Requirements

- Java 17+
- Maven
- A Discord bot token
- Multiple free APIs (e.g., NASA API, ISS tracking API)

### Setup

1. Clone the repository:
   `git clone https://github.com/jantrw/SpaceLinker.git`
   `cd SpaceLinker`

2. Create a `config.properties` file with the API keys and Discord token:
   `DISCORD_TOKEN=your_token_here`
   `NASA_API_KEY=your_nasa_api_key_here`
   `USERNAME=your_username`

3. Build and run with Maven:
   `mvn package`
   `java -jar target/SpaceLinker.jar`

## Usage

### Available Commands

| Command                | Description                                  |
| ---------------------- | -------------------------------------------- |
| `/iss`                 | Displays current ISS data                   |
| `/picture`             | Shows the NASA Astronomy Picture of the Day |
| `/pictureinfo`         | Shows the NASA Astronomy Picture with Info  |
| `/prefix <new_prefix>` | Changes the command prefix                  |

## Architecture

Core classes of the bot:

- `DiscordBot.java` – Starts the bot and registers commands.
- `BotListener.java` – Handles messages and slash commands.
- `GuildDataManager.java` – Stores guild-specific data.
- `ISSData.java` – Sends ISS data as a Discord message.
- `NasaPictureOfTheDay.java` – Sends the NASA Astronomy Picture of the Day as a Discord message.
- `JSONFetcherIss.java` – Fetches current ISS data from an API.
- `JSONFetcherNasa.java` – Fetches the NASA Picture of the Day.

## License

MIT License
