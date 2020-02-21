function PlayerController(leftJoystickController, rightJoystickController) {

    this.THRESHOLD = 0.01

    this.leftJoystickController = leftJoystickController;
    this.rightJoystickController = rightJoystickController;

    this.movingFraction = 0;
    this.strafingFraction = 0;
    this.horizontalSteeringFraction = 0;
    this.verticalSteeringFraction = 0;

    this.update = function() {
        if (Math.abs(this.leftJoystickController.joystickPositionY) >= this.THRESHOLD) {
            this.movingFraction = this.leftJoystickController.joystickPositionY;
        } else {
            this.movingFraction = 0;
        }
        if (Math.abs(this.leftJoystickController.joystickPositionX) >= this.THRESHOLD) {
            this.strafingFraction = this.leftJoystickController.joystickPositionX;
        } else {
            this.strafingFraction = 0;
        }

        if (Math.abs(this.rightJoystickController.joystickPositionX) >= this.THRESHOLD) {
            this.horizontalSteeringFraction = -this.rightJoystickController.joystickPositionX;
        } else {
            this.horizontalSteeringFraction = 0;
        }
        if (Math.abs(this.rightJoystickController.joystickPositionY) >= this.THRESHOLD) {
            this.verticalSteeringFraction = -this.rightJoystickController.joystickPositionY;
        } else {
            this.verticalSteeringFraction = 0;
        }
    }
}