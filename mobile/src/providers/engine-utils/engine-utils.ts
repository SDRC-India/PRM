import {
  Injectable
} from '@angular/core';
import {
  Token
} from '../../class/Token';
import {
  TokenChar
} from '../../class/TokenChar';

/*
  Generated class for the EngineUtilsProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class EngineUtilsProvider {

  OPERATORS: Map < String, number[] > = new Map();
  LEFT_ASSOC_ARRAY: number[] = []
  RIGHT_ASSOC_ARRAY: number[] = []
  LEFT_ASSOC: number = 0;
  RIGHT_ASSOC: number = 1;

  constructor() {
    this.LEFT_ASSOC_ARRAY.push(0)
    this.LEFT_ASSOC_ARRAY.push(this.LEFT_ASSOC)
    this.OPERATORS.set("&", this.LEFT_ASSOC_ARRAY);
    this.OPERATORS.set("|", this.LEFT_ASSOC_ARRAY);
  }

  resolveExpression(expression, questionMap, expressionType): any {

    let tokens: Token[] = this.tokenizer(expression, questionMap);
    let reversedTokens = this.InfixToRpnTransformer(tokens)
    let resultingTokens: Token = this.expressionResolver(reversedTokens, expressionType);

    if (resultingTokens.value)
      return resultingTokens.value.toString()
    return resultingTokens.value == null ? "null" : resultingTokens.value

  }


  tokenizer(s: String, questionMap: {}): Token[] {
    let letterBuffer: String[] = []
    let numberBuffer: String[] = []
    let result: Token[] = []

    let ss = s.replace(/\s/g, '')
    let str: String[] = ss.split("");

    for (let i = 0; i < str.length; i++) {

      let ch: string = str[i] as string;
      if (ch == '$') {
        let qName = "";
        let values: String[] = []
        for (let j = i + 2; j < str.length; j++) {
          if (str[j] == "}") {
            i = j;
            break;
          }
          qName = qName + (str[j]);
        }
        if (questionMap[qName].value) {
          // for (let ss of ((questionMap[qName].value) as string).split("")) {
          //   values.push(ss);
          // }
          values.push(questionMap[qName].value);
          if (values.length > 0) {
            values.forEach(num => {
              numberBuffer.push(num);
            });
          }
        } else {
          values.push('null');
          if (values.length > 0) {
            values.forEach(num => {
              numberBuffer.push(num);
            });
          }
        }

      } else if (new RegExp("\\d").test(ch)) {
        numberBuffer.push(ch);
      } else if (new RegExp("[.]").test(ch)) {
        numberBuffer.push(ch);
      } else if (new RegExp("[a-zA-Z]").test(ch)) {
        if (numberBuffer.length > 0) {
          if (numberBuffer.length > 0) {
            result.push(
              new Token(TokenChar.LITERAL, numberBuffer.join("")));
            numberBuffer = []
          }
          result.push(new Token(TokenChar.OPERATOR, "*"));
        }
        letterBuffer.push(ch);
      } else if (new RegExp("[-+*/%=<><=>=&|]").test(ch)) {

        if (numberBuffer.length > 0) {
          result.push(
            new Token(TokenChar.LITERAL, numberBuffer.join("")));
          numberBuffer = []
        }
        let l = letterBuffer.length;
        for (let i = 0; i < l; i++) {
          result.push(new Token(TokenChar.VARIABLE, letterBuffer[i]));
          if (i < l - 1) {
            result.push(new Token(TokenChar.OPERATOR, "*"));
          }
        }
        letterBuffer = []
        let ch1: string = str[i + 1] as string;
        if (ch1 == "=") {
          result.push(new Token(TokenChar.OPERATOR, ch + ch1));
          i = i + 1;
        } else {
          result.push(new Token(TokenChar.OPERATOR, ch));
        }

      } else if (new RegExp("[(]").test(ch)) {
        if (letterBuffer.length > 0) {
          result.push(new Token(TokenChar.FUNCTION,
            letterBuffer.join("")));
          letterBuffer = []
        } else if (numberBuffer.length > 0) {
          if (numberBuffer.length > 0) {
            result.push(
              new Token(TokenChar.LITERAL, numberBuffer.join("")));
            numberBuffer = []
          }
          result.push(new Token(TokenChar.OPERATOR, "*"));
        }
        result.push(new Token(TokenChar.LEFT_PARENTHESIS, ch));
      } else if (new RegExp("[)]").test(ch)) {
        let l = letterBuffer.length;
        for (let i = 0; i < l; i++) {
          result.push(new Token(TokenChar.VARIABLE, letterBuffer[i]));
          if (i < l - 1) {
            result.push(new Token(TokenChar.OPERATOR, "*"));
          }
        }
        letterBuffer = []
        if (numberBuffer.length > 0) {
          result.push(
            new Token(TokenChar.LITERAL, numberBuffer.join("")));
          numberBuffer = []
        }
        result.push(new Token(TokenChar.RIGHT_PARENTHESIS, ch));
      } else if (new RegExp("[,]").test(ch)) {
        if (numberBuffer.length > 0) {
          result.push(
            new Token(TokenChar.LITERAL, numberBuffer.join("")));
          numberBuffer = []
        }
        let l = letterBuffer.length;
        for (let i = 0; i < l; i++) {
          result.push(new Token(TokenChar.VARIABLE, letterBuffer[i]));
          if (i < l - 1) {
            result.push(new Token(TokenChar.OPERATOR, "*"));
          }
        }
        letterBuffer = []
        result.push(new Token(TokenChar.FUNCTION_ARGS_SEPARATOR, ch));
      }
    };

    if (numberBuffer.length > 0) {
      if (numberBuffer.length > 0) {
        result.push(
          new Token(TokenChar.LITERAL, numberBuffer.join("")));
        numberBuffer = []
      }
    }
    if (letterBuffer.length > 0) {
      let l = letterBuffer.length;
      for (let i = 0; i < l; i++) {
        result.push(new Token(TokenChar.VARIABLE, letterBuffer[i]));
        if (i < l - 1) {
          result.push(new Token(TokenChar.OPERATOR, "*"));
        }
      }
      letterBuffer = []
    }

    return result;
  }

  clearLetterBuffer(letterBuffer, result) {
    let l = letterBuffer.length;
    for (let i = 0; i < l; i++) {
      result.push(new Token(TokenChar.VARIABLE, letterBuffer[i]));
      if (i < l - 1) {
        result.push(new Token(TokenChar.OPERATOR, "*"));
      }
    }
    letterBuffer = []
  }

  clearNumberBuffer(numberBuffer, result) {
    if (numberBuffer.length > 0) {
      result.push(
        new Token(TokenChar.LITERAL, numberBuffer.join("")));
      numberBuffer = []
    }
  }

  pushIntoNumberBuffer(numbers: String[], numberBuffer) {
    if (numbers.length > 0) {
      numbers.forEach(num => {
        numberBuffer.push(num);
      });
    }
  }
  //   while there are tokens to be read:
  //   read a token.
  //   if the token is a number, then:
  //       push it to the output queue.
  //   if the token is a function then:
  //       push it onto the operator stack
  //   if the token is an operator, then:
  //       while ((there is a function at the top of the operator stack)
  //              or (there is an operator at the top of the operator stack with greater precedence)
  //              or (the operator at the top of the operator stack has equal precedence and is left associative))
  //             and (the operator at the top of the operator stack is not a left bracket):
  //           pop operators from the operator stack onto the output queue.
  //       push it onto the operator stack.
  //   if the token is a left bracket (i.e. "("), then:
  //       push it onto the operator stack.
  //   if the token is a right bracket (i.e. ")"), then:
  //       while the operator at the top of the operator stack is not a left bracket:
  //           pop the operator from the operator stack onto the output queue.
  //       pop the left bracket from the stack.
  //       /* if the stack runs out without finding a left bracket, then there are mismatched parentheses. */
  // if there are no more tokens to read:
  //   while there are still operator tokens on the stack:
  //       /* if the operator token on the top of the stack is a bracket, then there are mismatched parentheses. */
  //       pop the operator from the operator stack onto the output queue.
  // exit.

  InfixToRpnTransformer(tokens: Token[]): Token[] {

    let outputQueue: Token[] = []
    let opStack: Token[] = []
    let wereValuesStack: Token[] = [];
    let argStack: number[] = [];

    for (let v of tokens) {
      // if the token is a number, then: push it to the output queue.
      if (v.type == TokenChar.LITERAL) {
        outputQueue.push(v);
        if (wereValuesStack.length != 0) {
          wereValuesStack.pop();
          wereValuesStack.push(new Token(TokenChar.TRUE, "true"));
        }
      } else if (v.type == TokenChar.FUNCTION) {
        opStack.push(v);
        argStack.push(0);
        let temwereValuesStack: Token[] = []
        if (wereValuesStack.length != 0) {
          wereValuesStack.pop();
          temwereValuesStack.push(new Token(TokenChar.TRUE, "true"));
        }
        temwereValuesStack.forEach(element => {
          wereValuesStack.push(element)
        });;
        wereValuesStack.push(new Token(TokenChar.FALSE, "false"));
      } else if (v.type == TokenChar.LEFT_PARENTHESIS) {
        opStack.push(v);
      } else if (v.type == TokenChar.RIGHT_PARENTHESIS) {

        while (opStack.length != 0 && opStack[opStack.length - 1].type == TokenChar.OPERATOR && !(opStack[opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS)) {
          outputQueue.push(opStack.pop());
        }
        if (opStack.length == 0) {
          // console.error("Invalid expressions !");
          throw Error("Invalid Expression")
        }

        opStack.pop();
        // if (this.opStack.length != 0 && this.opStack[this.opStack.length - 1].type == TokenChar.FUNCTION) {
        //     this.outputQueue.push(this.opStack.pop());
        // }
        if (opStack.length != 0 && opStack[opStack.length - 1].type == TokenChar.FUNCTION) {
          // outputQueue.push(opStack.pop());
          let f: Token = opStack.pop();
          let argsCount = argStack.pop();
          let hasValues: Token = wereValuesStack.pop();
          if (hasValues.type == TokenChar.TRUE) {
            argsCount++;
          }
          f.arguments = argsCount;
          outputQueue.push(f);
        }
      } else if (v.type == TokenChar.OPERATOR) {
        while (opStack.length != 0 && (opStack[opStack.length - 1].type == TokenChar.OPERATOR) &&
          ((v.associativity() == "left" && (v.precedence() <= opStack[opStack.length - 1].precedence())) ||
            (v.associativity() == "right" &&
              v.precedence() < opStack[opStack.length - 1].precedence()))) {
          outputQueue.push(opStack.pop());
        }
        opStack.push(v);
      } else if (v.type == TokenChar.FUNCTION_ARGS_SEPARATOR) {
        while (opStack.length != 0 && !(opStack[opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS)) {
          outputQueue.push(opStack.pop());
        }
        if (wereValuesStack.length == 0) {
          // console.log("Opstack:", opStack, "OutputQueue:", outputQueue)
        }
        if (wereValuesStack.pop().type == TokenChar.TRUE) {
          let argsc = argStack.pop();
          argsc++;
          argStack.push(argsc);
        }
        wereValuesStack.push(new Token(TokenChar.FALSE, "false"));
      }
    }
    if (opStack.length != 0 && opStack[opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS) {
      // console.error("Invalid expressions !");
      throw Error("Invalid Expression")
    }
    opStack = opStack.reverse();
    for (let op of opStack) {
      outputQueue.push(op);
    }
    return outputQueue;
  }


  expressionResolver(tokens: Token[], expressionType): Token {
    let rstack: Token[] = []
    // For each token
    for (let token of tokens) {
      // If the token is a value push it onto the stack

      switch (token.type) {

        case TokenChar.LITERAL:
          rstack.push(token);
          break;
        case TokenChar.FUNCTION:
          switch (token.value.toString()) {
            case "clone":
              let val: Token = rstack.pop();
              rstack.push(new Token(TokenChar.RESULT, val.value));
              break
            case "binand":
              {
                let res = 1;
                let args = token.arguments,
                  argc = 0;

                while (argc < args && rstack.length != 0 && (rstack[rstack.length - 1].type == TokenChar.LITERAL || rstack[rstack.length - 1].type == TokenChar.RESULT)) {
                  let val = rstack.pop();
                  if (val.value != null && val.value != "null") {
                    res = res * Number(val.value);
                  } else {
                    res = 0;
                    break
                  }
                  argc++;
                }
                rstack.push(new Token(TokenChar.RESULT, res));
              }
              break;
            case "sum":
              {
                let sum = NaN
                let farguments = token.arguments;
                do {
                  farguments--;
                  let val: Token = rstack.pop();
                  if (val.value != null && val.value != "null" && isNaN(sum))
                    sum = +0
                  if (val.value != null && val.value != "null") {
                    sum = +sum + +val.value
                  }
                } while (farguments >= 1)

                rstack.push(new Token(TokenChar.RESULT, sum));
              }
              break;
            case "minus":
              {
                // BigDecimal minus = BigDecimal.ZERO;
                let minus = NaN
                while (!(rstack.filter(f => (f.type == TokenChar.LITERAL)).length == 0)) {
                  let val: Token = rstack.pop();
                  if (val.value != null && val.value != "null" && isNaN(minus))
                    minus = +0
                  if (val.value != null && val.value != "null") {
                    minus = +val.value - +minus
                  }
                }
                rstack.push(new Token(TokenChar.RESULT, minus));
              }
              break;
            case "mul":
              {
                // BigDecimal mul = BigDecimal.ONE;
                let mul: any = 1
                while (!(rstack.filter(f => (f.type == TokenChar.LITERAL)).length == 0)) {
                  let val: Token = rstack.pop();
                  if (val.value != null) {
                    mul = +mul * +val.value
                  }
                }
                rstack.push(new Token(TokenChar.RESULT, mul));

              }
              break;
            case "div":
              {
                if (token.arguments == 2) {
                  let denominator: Token = rstack.pop();
                  let numerator: Token = rstack.pop();
                  if (denominator.value == "null" || denominator.value == null || denominator.value == "" || denominator.value == "0" || denominator.value == NaN ||
                    numerator.value == "null" || numerator.value == null || numerator.value == "" || numerator.value == NaN) {
                    // throw new Error("Division not possible, denominator can't be null");
                    rstack.push(new Token(TokenChar.RESULT, null));
                  } else {
                    if (numerator.value != "null" && typeof numerator.value == "string") {
                      numerator.value = parseInt(numerator.value)
                    }
                    if (denominator.value != "null" && typeof denominator.value == "string") {
                      denominator.value = parseInt(denominator.value)
                    }
                    if (denominator.value != null || numerator.value != null) {
                      rstack.push(new Token(TokenChar.RESULT, (numerator.value / denominator.value)));
                    }
                  }
                } else {
                  throw new Error("Invalid number of arguments for division!")
                }
              }
              break;
            case "perc":
              {
                if (token.arguments == 2) {
                  let t1: Token = rstack.pop();
                  let t2: Token = rstack.pop();
                  if (t2.value == null) {
                    // throw new Error("Division not possible, denominator can't be null");
                    rstack.push(new Token(TokenChar.RESULT, null));
                  }
                  if (t1.value != null || t2.value != null) {
                    let i1 = (t1.value.toString());
                    let i2 = (t2.value.toString());
                    rstack.push(new Token(TokenChar.RESULT, +i2 / +i1));
                  }
                } else if (token.arguments == 3) {
                  // ;
                  let precision: Token = rstack.pop();
                  let denominator: Token = rstack.pop();
                  let numerator: Token = rstack.pop();
                  if (denominator.value == "null" || denominator.value == null || denominator.value == "" || denominator.value == NaN ||
                    numerator.value == "null" || numerator.value == null || numerator.value == "" || numerator.value == NaN) {
                    // throw new Error("Division not possible, denominator can't be null");
                    rstack.push(new Token(TokenChar.RESULT, null));
                  } else {
                    if (numerator.value != "null" && typeof numerator.value == "string") {
                      numerator.value = parseInt(numerator.value)
                    }
                    if (denominator.value != "null" && typeof denominator.value == "string") {
                      denominator.value = parseInt(denominator.value)
                    }
                    if (denominator.value != null || numerator.value != null) {
                      rstack.push(new Token(TokenChar.RESULT, (Math.round(numerator.value / denominator.value * 100 * 10) / 10).toFixed(precision.value)));
                    }
                  }
                } else {
                  throw new Error("Invalid number of arguments for division!")
                }
              }
              break;
            case "round":
              {
                let rounder: Token = rstack.pop();
                let t1 = rstack.pop();
                rstack.push(new Token(TokenChar.LITERAL, t1.value.round(rounder.value as any)));
              }
              break;
            case "lessThan":
              {
                let less1 = rstack.pop()
                let less2 = rstack.pop()
                if (less1.value != null && less1.value != "null" && less1.value != "" && typeof less1.value == 'string') {
                  less1.value = Number(less1.value)
                }
                if (less2.value != null && less2.value != "null" && less2.value != "" && typeof less2.value == 'string') {
                  less2.value = Number(less2.value)
                }
                if (less1.value != null && less1.value != "null" && less1.value != "" && less2.value != null && less2.value != "null" && less2.value != "" && (less2.value <= less1.value)) {
                  rstack.push(new Token(TokenChar.RESULT, 1));
                } else {
                  rstack.push(new Token(TokenChar.RESULT, 0));
                }
              }
              break;
            case "if":
              {
                if (token.arguments == 0 || token.arguments == 1) {
                  if (expressionType.equals("score")) {
                    let scoreFailure = rstack.pop();
                    let scoreSucess = rstack.pop();
                    let valueOfControlAfterConditionResolved = rstack.pop();
                    if (valueOfControlAfterConditionResolved.value == null || valueOfControlAfterConditionResolved.value == "null") {
                      rstack.push(new Token(TokenChar.RESULT, "null"));
                    } else if (Number(valueOfControlAfterConditionResolved.value) == 1) {
                      rstack.push(new Token(TokenChar.RESULT, (scoreSucess.value)));
                    } else {
                      rstack.push(new Token(TokenChar.RESULT, (scoreFailure.value)));
                    }
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, rstack.pop().value));
                  }
                } else if (token.arguments == 2) {
                  let scoreFailure = rstack.pop()
                  let scoreSucess = rstack.pop()
                  let valueOfControlAfterConditionResolved = rstack.pop()
                  if (valueOfControlAfterConditionResolved.value == null) {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (valueOfControlAfterConditionResolved.value == 1) {
                    rstack.push(new Token(TokenChar.RESULT, parseInt(scoreSucess.value)));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, parseInt(scoreFailure.value)));
                  }
                } else if (token.arguments == 3) {
                  let scoreFailure = rstack.pop()
                  let scoreSucess = rstack.pop()
                  let valueOfControlAfterConditionResolved = rstack.pop()
                  if (valueOfControlAfterConditionResolved.value == null || valueOfControlAfterConditionResolved.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (valueOfControlAfterConditionResolved.value == 1) {
                    rstack.push(new Token(TokenChar.RESULT, parseInt(scoreSucess.value)));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, parseInt(scoreFailure.value)));
                  }
                }
              }
              break;
            case "selected":
              {
                let valueOfControl = rstack.pop()
                let valueToMatch = rstack.pop()
                if (valueOfControl.value == valueToMatch.value) {
                  rstack.push(new Token(TokenChar.RESULT, 1));
                } else {
                  rstack.push(new Token(TokenChar.RESULT, 0));
                }
              }
              break;
          }
          break;
        case TokenChar.OPERATOR:
          {
            switch (token.value) {
              case "+":
                switch (expressionType) {
                  case "constraint":
                    {
                      let sum: any = 1

                      while (!(rstack.filter(f => (f.type == TokenChar.LITERAL)).length == 0)) {
                        let val: Token = rstack.pop();
                        if (val.value != null) {
                          sum = +sum * +val.value
                        }
                      }
                    }
                    break;
                  default:
                    {
                      let sum = NaN
                      while (!(rstack.filter(f => (f.type == TokenChar.LITERAL || f.type == TokenChar.RESULT)).length == 0)) {
                        let val: Token = rstack.pop();
                        if (val.value != null && val.value != "null" && isNaN(sum))
                          sum = +0
                        if (val.value != null && val.value != "null") {
                          sum = +sum + +val.value
                        }
                      }
                      rstack.push(new Token(TokenChar.RESULT, sum));
                    }

                }

                break;
              case "-":
                {
                  let minus = NaN
                  while (!(rstack.filter(f => (f.type == TokenChar.LITERAL || f.type == TokenChar.RESULT)).length == 0)) {
                    let val: Token = rstack.pop();
                    if (val.value != null && val.value != "null" && isNaN(minus))
                      minus = +0
                    if (val.value != null && val.value != "null") {
                      minus = +val.value - +minus
                    }
                  }
                  rstack.push(new Token(TokenChar.RESULT, minus));
                }
                break;
              case "*":

                break;
              case "/":

                break;
              case "<":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value != null || (value1.value != "null" && typeof value1.value == "string")) {
                    value1.value = Number(value1.value)
                  }
                  if (value2.value != null || (value2.value != "null" && typeof value2.value == "string")) {
                    value2.value = Number(value2.value)
                  }

                  if (value1.value == null || value1.value == "null" || value2.value == null || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (Number(value2.value) < Number(value1.value)) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
              case ">":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value != null || (value1.value != "null" && typeof value1.value == "string")) {
                    value1.value = Number(value1.value)
                  }
                  if (value2.value != null || (value2.value != "null" && typeof value2.value == "string")) {
                    value2.value = Number(value2.value)
                  }


                  if (value1.value == null || value1.value == "null" || value2.value == null || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (Number(value2.value) > Number(value1.value)) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
              case "<=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value != null || (value1.value != "null" && typeof value1.value == "string")) {
                    value1.value = Number(value1.value)
                  }
                  if (value2.value != null || (value2.value != "null" && typeof value2.value == "string")) {
                    value2.value = Number(value2.value)
                  }

                  if (value1.value == null || value1.value == "null" || value2.value == null || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (Number(value2.value) <= Number(value1.value)) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
              case ">=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value != null || (value1.value != "null" && typeof value1.value == "string")) {
                    value1.value = Number(value1.value)
                  }
                  if (value2.value != null || (value2.value != "null" && typeof value2.value == "string")) {
                    value2.value = Number(value2.value)
                  }

                  if (value1.value == null || value1.value == "null" || value2.value == null || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else {
                    if (value1.value != "null" && typeof value1.value == "string") {
                      value1.value = Number(value1.value)
                    }
                    if (value2.value != "null" && typeof value2.value == "string") {
                      value2.value = Number(value2.value)
                    }

                    if (value1.value == "null" || value2.value == "null") {
                      rstack.push(new Token(TokenChar.RESULT, "null"));
                    } else if (Number(value2.value) >= Number(value1.value)) {
                      rstack.push(new Token(TokenChar.RESULT, 1));
                    } else {
                      rstack.push(new Token(TokenChar.RESULT, 0));
                    }
                  }

                }
                break;
              case "=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value == "null" || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (Number(value1.value) == Number(value2.value)) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
              case "&":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value == "null" || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (Number(value1.value) == 1 && Number(value2.value) == 1) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
              case "|":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value == "null" || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (Number(value1.value) == 1 || Number(value2.value) == 1) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
            }
          }
        default:
          break;
      }
      // console.log("Token--RR::" + token.print() + "\n", JSON.stringify(rstack), "\n\n");
    }
    return rstack.pop();
  }



  isOperator(token: String): boolean {
    return this.OPERATORS.has(token);
  }

  isAssociative(token: String, type: number): boolean {
    if (!this.isOperator(token)) {
      throw new Error("Token is invalid: " + token);
    }
    if (this.OPERATORS.get(token)[1] == type) {
      return true;
    }
    return false;
  }

  // Compare precedence of operators.
  compareOperatorPrecedence(token1: String, token2: String): number {
    if (!this.isOperator(token1) || !this.isOperator(token2)) {
      throw new Error("Tokens are invalid: " + token1 + " " + token2);
    }
    return this.OPERATORS.get(token1)[0] - this.OPERATORS.get(token2)[0];
  }

  // Convert infix expression format into reverse Polish notation
  transformInfixToReversePolishNotationForRelevance(inputTokens: String[]): String[] {
    let outputArray: String[] = []
    let stack: String[] = []

    for (let token of inputTokens) {
      if (this.isOperator(token)) {
        while (!(stack.length == 0) && this.isOperator(stack[stack.length - 1])) {
          if ((this.isAssociative(token, this.LEFT_ASSOC) && this.compareOperatorPrecedence(token, stack[stack.length - 1]) <= 0) ||
            (this.isAssociative(token, this.RIGHT_ASSOC) && this.compareOperatorPrecedence(token, stack[stack.length - 1]) < 0)) {

            outputArray.push(stack.pop());
            continue;
          }
          break;
        }
        stack.push(token);
      } else if (token == "(") {
        stack.push(token);
      } else if (token == ")") {
        while (!(stack.length == 0) && !(stack[stack.length - 1] == "(")) {
          outputArray.push(stack.pop());
        }
        stack.pop();
      } else {
        outputArray.push(token);
      }
    }
    while (!(stack.length == 0)) {
      outputArray.push(stack.pop());
    }
    return outputArray;
  }

  arithmeticExpressionResolverForRelevance(tokens: String[]): boolean {

    let stack: String[] = []
    // For each token
    for (let token of tokens) {
      // If the token is a value push it onto the stack
      if (token == "")
        continue;
      if (!this.isOperator(token)) {
        stack.push(token);
      } else {
        // Token is an operator: pop top two entries
        let d2: Number = new Number(stack.pop());
        let d1: Number = new Number(stack.pop());

        // Get the result
        switch (token) {
          case "&":
            if (d1 == 1 && d2 == 1) {
              stack.push(new String(1));
            } else
              stack.push(new String(0));
            break;
          case "|":
            if (d1 == 1 || d2 == 1) {
              stack.push(new String(1));
            } else {
              stack.push(new String(0));
            }
            break;

        }
      }
    }
    return new Number(stack.pop()) == 1 ? true : false;
  }


  expressionToArithmeticExpressionTransfomerForRelevance(expression: String, questionMap,arrayIndex?:number): String {

    let expressionTransformed = "";
    for (let str of expression.split("}")) {
      let expressions: String[] = str.split(":");
      for (let i = 0; i < expressions.length; i++) {
        let exp: String = (expressions[i]);
        switch (exp) {
          case "@@split":
            expressionTransformed = expressionTransformed + " & ";
            break;
          case "@@or":
            expressionTransformed = expressionTransformed + " | ";
            break;
          case "@AND@":
            expressionTransformed = expressionTransformed + " & ";
            break;
          case "@AND":
            expressionTransformed = expressionTransformed + " & ";
            break;
          case "@OR":
            expressionTransformed = expressionTransformed + " | ";
            break;
          case "{":
            expressionTransformed = expressionTransformed + "";
            break;
          case "}":
            expressionTransformed = expressionTransformed + "";
            break;
          case "":
            break;
          default:
            if (exp.includes("(")) {
              expressionTransformed = expressionTransformed + " ( ";
            } else if (exp.includes(")")) {
              expressionTransformed = expressionTransformed + " ) ";
            }
            break;
          case "optionEquals":
          case "otherOptionEquals":
            {
              // find the type detail Id
              // if null or value in expression doesn't match match value selected in UI push
              // 0
              // else push 1
              {
                let qName = (expressions[i - 1]);
                if (questionMap[qName as any].controlType == 'autoCompleteMulti' ) {
                  let typeDetailsId = parseInt(expressions[i + 1] as string);
                  if (questionMap[qName as any].value && questionMap[qName as any].value.length > 0) {
                    for (let v of questionMap[qName as any].value) {
                      if (v.key == typeDetailsId) {
                        expressionTransformed = expressionTransformed + " 1 ";
                        break;
                      } else {
                        expressionTransformed = expressionTransformed + " 0 ";
                      }
                    }
                  } else {
                    expressionTransformed = expressionTransformed + " 0 ";
                  }
                  i = i + 1;

                }
                else if (questionMap[qName as any].controlType == 'dropdown' && questionMap[qName as any].type == 'checkbox' ) {
                  let typeDetailsId = parseInt(expressions[i + 1] as string);
                  if (questionMap[qName as any].value) {
                    for (let v of questionMap[qName as any].value) {
                      if (v == typeDetailsId) {
                        expressionTransformed = expressionTransformed + " 1 ";
                        break;
                      } else {
                        expressionTransformed = expressionTransformed + " 0 ";
                      }
                    }
                  } else {
                    expressionTransformed = expressionTransformed + " 0 ";
                  }
                  i = i + 1;
                }else if (questionMap[qName as any].controlType == 'autoCompleteTextView' && questionMap[qName as any].type == 'option' ) {
                  let typeDetailsId = parseInt(expressions[i + 1] as string);

                  if (questionMap[qName as any].value && questionMap[qName as any].value.key == typeDetailsId) {
                    expressionTransformed = expressionTransformed + " 1 ";
                  } else {
                    expressionTransformed = expressionTransformed + " 0 ";
                  }
                  i = i + 1;
                
                 } else {
                  //handling dropdodwn
                  let typeDetailsId = parseInt(expressions[i + 1] as string);

                  if (questionMap[qName as any].value == typeDetailsId) {
                    expressionTransformed = expressionTransformed + " 1 ";
                  } else {
                    expressionTransformed = expressionTransformed + " 0 ";
                  }
                  i = i + 1;
                }

              }
            }
            break;
          case "optionEqualsMultiple":
            // find the type detail Id
            // if null or value in expression doesn't match match value selected in UI push
            // 0
            // else push 1
            {
              let typeDetailsIds: string[] = expressions[i + 1].split(",");
              let qName = expressions[i - 1];
              let matches = 0;
              if (questionMap[qName as any].value != null){
              for (let typeDetailsId of typeDetailsIds) {
                if (questionMap[qName as any].value.indexOf(parseInt(typeDetailsId)) != -1 ) {
                  matches = 1;
                  break
                }
              }
            }
              if (matches == 1) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "textEquals":
            {
              let num = (expressions[i + 1]);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null && (questionMap[qName as any].value as string) == num) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "numEquals":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) == (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "greaterThan":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) > (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "greaterThanEquals":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) >= (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "lessThan":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) < (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "lessThanEquals":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) <= (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
            case "filterOption":
            {
              let parent = (expressions[i - 1]);
              let child = (expressions[i + 1]);
              let childKey = child.split(".")[1]
              let childColName = child.split(".")[0]
              let option =  questionMap[childColName].options[arrayIndex];
              // console.log(
              //   exp,
              //   "\n parent:" + questionMap[parent as any],
              //   "\n child:" +questionMap[childColName as any],
              //   "\n option:" + option,
              //   "\n parent value:" + questionMap[parent as any].value,
              //   "\n child key:" + childKey,
              //   "\n child value:" +  option['extraKeyMap'][childKey],
              //   questionMap[parent as any].value == option['extraKeyMap'][childKey])

                if(questionMap[parent as any].value == option['extraKeyMap'][childKey]){
                  expressionTransformed = expressionTransformed + " 1 ";
                }else{
                  expressionTransformed = expressionTransformed + " 0 ";
                }
            }
            break;
            case "filterOptionByValue":
            {
              let child = (expressions[i + 1]);
              let value = (expressions[i + 2]);
              let childKey = child.split(".")[1]
              let childColName = child.split(".")[0]
              let option =  questionMap[childColName].options[arrayIndex];

                if(String(value) == String(option['extraKeyMap'][childKey])){
                  expressionTransformed = expressionTransformed + " 1 ";
                }else{
                  expressionTransformed = expressionTransformed + " 0 ";
                }
            }
            break;
        }
      }
    }
    return expressionTransformed;
  }



  constraintExpressionResolver(tokens: Token[]): Token {
    let rstack: Token[] = []

    // For each token
    for (let token of tokens) {
      // If the token is a value push it onto the stack
      // console.log("Token::" + token);

      switch (token.type) {

        case TokenChar.LITERAL:
          rstack.push(token);
          break;
        case TokenChar.FUNCTION:
          switch (token.value.toString()) {

            case "sum":
              {
                let sum = NaN
                let farguments = token.arguments;
                do {
                  farguments--;
                  let val: Token = rstack.pop();
                  if (val.value != null && val.value != "null" && isNaN(sum))
                    sum = +0
                  if (val.value != null && val.value != "null") {
                    sum = +sum + +val.value
                  }
                } while (farguments >= 1)

                rstack.push(new Token(TokenChar.RESULT, sum));
              }
              break;
            case "minus":
              {
                // BigDecimal minus = BigDecimal.ZERO;
                let minus = NaN
                while (!(rstack.filter(f => (f.type == TokenChar.LITERAL)).length == 0)) {
                  let val: Token = rstack.pop();
                  if (val.value != null && val.value != "null" && isNaN(minus))
                    minus = +0
                  if (val.value != null && val.value != "null") {
                    minus = +val.value - +minus
                  }
                }
                rstack.push(new Token(TokenChar.RESULT, minus));
              }
              break;
            case "mul":
              {
                // BigDecimal mul = BigDecimal.ONE;
                let mul: any = 1
                while (!(rstack.filter(f => (f.type == TokenChar.LITERAL)).length == 0)) {
                  let val: Token = rstack.pop();
                  if (val.value != null) {
                    mul = +mul * +val.value
                  }
                }
                rstack.push(new Token(TokenChar.RESULT, mul));

              }
              break;
            case "div":
              {
                let t1: Token = rstack.pop();
                let t2: Token = rstack.pop();
                if (t2.value == null) {
                  throw new Error("Division not possible, denominator can't be null");
                }
                if (t1.value != null || t2.value != null) {
                  let i1 = (t1.value.toString());
                  let i2 = (t2.value.toString());
                  rstack.push(new Token(TokenChar.LITERAL, +i2 / +i1));
                }
              }
              break;
            case "round":
              {
                let rounder: Token = rstack.pop();
                let t1 = rstack.pop();
                rstack.push(new Token(TokenChar.LITERAL, t1.value.round(rounder.value as any)));
              }
              break;
            case "lessThan":
              {
                let less1 = rstack.pop()
                let less2 = rstack.pop()
                if (less1.value != null && less1.value != "null" && less1.value != "" && typeof less1.value == 'string') {
                  less1.value = Number(less1.value)
                }
                if (less2.value != null && less2.value != "null" && less2.value != "" && typeof less2.value == 'string') {
                  less2.value = Number(less2.value)
                }
                if (less1.value != null && less1.value != "null" && less1.value != "" && less2.value != null && less2.value != "null" && less2.value != "" && (less2.value <= less1.value)) {
                  rstack.push(new Token(TokenChar.RESULT, 1));
                } else {
                  rstack.push(new Token(TokenChar.RESULT, 0));
                }
              }
              break;
            case "if":
              {
                if (token.arguments == 0) {
                  rstack.push(new Token(TokenChar.RESULT, rstack.pop()));
                } else if (token.arguments == 2) {
                  let scoreFailure = rstack.pop()
                  let scoreSucess = rstack.pop()
                  let valueOfControlAfterConditionResolved = rstack.pop()
                  if (valueOfControlAfterConditionResolved.value == null) {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (valueOfControlAfterConditionResolved.value == 1) {
                    rstack.push(new Token(TokenChar.RESULT, parseInt(scoreSucess.value)));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, parseInt(scoreFailure.value)));
                  }
                }
              }
              break;
            case "selected":
              {
                let valueOfControl = rstack.pop()
                let valueToMatch = rstack.pop()
                if (valueOfControl.value == valueToMatch.value) {
                  rstack.push(new Token(TokenChar.RESULT, 1));
                } else {
                  rstack.push(new Token(TokenChar.RESULT, 0));
                }
              }
              break;
          }
          break;
        case TokenChar.OPERATOR:
          {
            switch (token.value) {
              case "+":
                let sum = NaN
                while (!(rstack.filter(f => (f.type == TokenChar.LITERAL || f.type == TokenChar.RESULT)).length == 0)) {
                  let val: Token = rstack.pop();
                  if (val.value != null && val.value != "null" && isNaN(sum))
                    sum = +0
                  if (val.value != null && val.value != "null") {
                    sum = +sum + +val.value
                  }
                }
                rstack.push(new Token(TokenChar.RESULT, sum));
                break;
              case "-":
                {
                  let minus = NaN
                  while (!(rstack.filter(f => (f.type == TokenChar.LITERAL || f.type == TokenChar.RESULT)).length == 0)) {
                    let val: Token = rstack.pop();
                    if (val.value != null && val.value != "null" && isNaN(minus))
                      minus = +0
                    if (val.value != null && val.value != "null") {
                      minus = +val.value - +minus
                    }
                  }
                  rstack.push(new Token(TokenChar.RESULT, minus));
                }
                break;
              case "*":

                break;
              case "/":

                break;
              case "<":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value != "null" && typeof value1.value == "string") {
                    value1.value = parseInt(value1.value)
                  }
                  if (value2.value != "null" && typeof value2.value == "string") {
                    value2.value = parseInt(value2.value)
                  }

                  if (value1.value == "null" || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (value1.value < value2.value) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
              case ">":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value != "null" && typeof value1.value == "string") {
                    value1.value = parseInt(value1.value)
                  }
                  if (value2.value != "null" && typeof value2.value == "string") {
                    value2.value = parseInt(value2.value)
                  }


                  if (value1.value == "null" || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (value2.value > value1.value) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
              case "<=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value != "null" && typeof value1.value == "string") {
                    value1.value = parseInt(value1.value)
                  }
                  if (value2.value != "null" && typeof value2.value == "string") {
                    value2.value = parseInt(value2.value)
                  }

                  if (value1.value == "null" || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (value2.value <= value1.value) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
              case ">=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value == "null" || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (value2.value >= value1.value) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
              case "=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if (value1.value == "null" || value2.value == "null") {
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  } else if (value1.value == value2.value) {
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  } else {
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
            }
          }
        default:
          break;
      }
    }
    return rstack.pop();
  }

}
