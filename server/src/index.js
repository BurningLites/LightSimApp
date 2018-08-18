import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';

class ControlPanel extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      isRunning: false,
      isPaused: false,
      isScheduled: false,
      currentProgram: "wave",
      programNames: [],
    };
    this.sendControlRequest("status");
  }
 
  render() {
    let status = "";
    if (this.state.isRunning) {
      status = "Currently running \"" + this.state.currentProgram + "\".";
    } else if (this.state.isPaused) {
      status = "Currently paused.";
    } else {
      status = "Currently stopped.";
    }

    const timeFormat = Intl.DateTimeFormat('en-us', {hour: 'numeric', minute: 'numeric'});

    if (this.state.isScheduled) {
      const nextState = this.state.nextEventType === "SUNRISE" ? "stop" : "start";
      const nextEventDate = new Date(this.state.nextEventTime);
      status += "\nLights are scheduled to " + nextState + " at " + timeFormat.format(nextEventDate) + ".";
    } else {
      status += "\nLights are not scheduled.";
    }

    if (this.state.currentTime !== undefined) {
      const currentTime = new Date(this.state.currentTime);
      status += "\nCurrent time is " + timeFormat.format(currentTime) + ".";
    }

    const playButtonImage = this.state.isRunning ?
        "baseline-pause-24px.svg" :
        "baseline-play_arrow-24px.svg";
    const playButtonAlt = this.state.isRunning ?
        "Pause" :
        "Play";

    return (
      <div className="container">
        <div className="title">
          Gym Lights
        </div>
        <div className="status">
          {status}
        </div>
        <div className="button-container">
          <img className="play-buttons"
              src="baseline-stop-24px.svg"
              alt="Stop"
              onClick={() => this.handleStopClick()}
              style={{width: 40,
                      height: 40,
                      tintColor: 'red',
                    }}
              />
          <img className="play-buttons"
              src={playButtonImage}
              alt={playButtonAlt}
              onClick={() => this.handlePlayClick()}
              />
        </div>
        <label className="scheduled-checkbox-label">
          Scheduled
          <input
            className="scheduled-checkbox"
            name="Scheduled"
            type="checkbox"
            checked={this.state.isScheduled}
            onChange={() => this.handleScheduledClick()} />
        </label>
        <select className="program-select" value={this.state.currentProgram} onChange={(event) => this.handleProgramSelect(event.target.value)}>
          { this.state.programNames.map((name, i) => <option key={name} value={name}>{name}</option>)}
        </select>
      </div>
    );
  }

  handleStopClick() {
    console.log("Stop button clicked.");
    this.sendControlRequest("stop");
  }

  handlePlayClick() {
    console.log("Play/Pause button clicked.");
    const command = this.state.isRunning ? "pause" : "start";
    this.sendControlRequest(command);
  }

  handleScheduledClick() {
    console.log("Scheduled checkbox clicked.");
    const command = this.state.isScheduled ? "unschedule" : "schedule";
    this.sendControlRequest(command);
  }

  handleProgramSelect(programName) {
    console.log('Program "' + programName + '" selected.');
    this.sendControlRequest("program?name=" + programName);
  }

  sendControlRequest(command) {
    fetch("http://" + window.location.hostname + ":8000/control/" + command)
      .then(res => res.json())
      .then((result) => {
        console.log("Response from control server: " + JSON.stringify(result));
        this.setStateFromResponse(result);
      },
      (error) => {
        console.log("Error in response: " + error);
      });
  }

  setStateFromResponse(jsonResponse) {
    this.setState({
      isRunning: jsonResponse.running,
      currentProgram: jsonResponse.currentProgram,
      isScheduled: jsonResponse.scheduled,
      currentTime: jsonResponse.currentTime,
      nextEventType: jsonResponse.nextEventType,
      nextEventTime: jsonResponse.nextEventTime,
      programNames: jsonResponse.controllers,
    });
  }

  handleClick(i) {
    this.setState({
    });
  }
}

// ========================================

ReactDOM.render(
  <ControlPanel />,
  document.getElementById('root')
);
