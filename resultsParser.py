import re
import time
import matplotlib.pyplot as plt
import statistics
import sys

BOT = ''


def main():
    qlearning_results = ExperimentResults.from_log('result' + BOT + '.txt')

    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1)
    ax.set_xlabel('battle (1000 rounds)')
    ax.set_ylabel('Percent of points')
    fig.tight_layout()

    ax.plot(qlearning_results.percentage, linewidth=0.6, label='QLearning', color='tab:blue')
    ax.plot(qlearning_results.running_avg, linewidth=0.6, label='Running average', color='tab:orange')
    # plt.legend()
    # plt.show()
    plt.savefig('QLearningResults' + BOT, dpi=300)


class ExperimentResults:
    q_learning_robot_string = 'iwium.QLearningRobot'
    log_line = re.compile(r'.*?([A-Za-z0-9.]+)(?:\**)\t(\d+) \((\d+)%\).*')
    # 3rd: iwium.QLearningRobot*	41983 (9%)	19200	160	13578	260	8513	272	9	367	624

    def __init__(self):
        self.percentage = []
        self.running_avg = []

    def _running(self):
        for i in range(1, len(self.percentage)+1):
            beginning = max(0, i - 100)
            self.running_avg.append(statistics.mean(self.percentage[beginning:i]))

    @staticmethod
    def from_log(file: str):
        results = ExperimentResults()
        count_to_three = 0
        q_result = 0
        sum_of_scores = 0
        with open(file, 'r') as log:
            for line in log.readlines():
                matched_line = ExperimentResults.log_line.match(line)
                if matched_line is not None:
                    count_to_three += 1
                    score = float(matched_line.group(2))
                    robot = matched_line.group(1)
                    sum_of_scores += score
                    if robot == (ExperimentResults.q_learning_robot_string + BOT):
                        q_result = score
                    if count_to_three == 3:
                        results.percentage.append(100 * q_result / sum_of_scores)
                        q_result = 0
                        sum_of_scores = 0
                        count_to_three = 0
        results._running()
        return results


if __name__ == '__main__':
    if len(sys.argv) > 1:
        BOT = '2'
    main()
