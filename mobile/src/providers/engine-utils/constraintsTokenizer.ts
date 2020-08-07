import { Token } from "../../class/Token";
import { TokenChar } from "../../class/TokenChar";
import { Injectable } from "@angular/core";

@Injectable()
export class ConstraintTokenizer{
    result: Token[] = []
    letterBuffer: String[] = []
    numberBuffer: String[] = []
    outputQueue: Token[] = []
    opStack: Token[] = []
    wereValuesStack : Token[] = [];
      argStack : number[] = [];

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

      this.result = []
      this.letterBuffer = []
      this.numberBuffer = []

      this.outputQueue = []
      this.opStack = []
      this.wereValuesStack = []
      this.argStack = []

      let tokens: Token[] = this.tokenizer(expression, questionMap);
      let reversedTokens = this.InfixToRpnTransformer(tokens)
      let resultingTokens:Token =  this.expressionResolver(reversedTokens,expressionType);

      if(resultingTokens.value)
      return  resultingTokens.value.toString()
      return resultingTokens.value == null ? "null" : resultingTokens.value

    }

    tokenizer(s : string, questionMap){
        let  ss = s.replace(" ", "");
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
              values.push(questionMap[qName].value);
              this.pushIntoNumberBuffer(values);
            } else {
              values.push('null');
              this.pushIntoNumberBuffer(values);
            }

          } else if (new RegExp("\\d").test(ch)) {
            this.numberBuffer.push(ch);
          } else if (new RegExp("[.]").test(ch)) {
            this.numberBuffer.push(ch);
          } else if (new RegExp("[a-zA-Z]").test(ch)) {
            if (this.numberBuffer.length > 0) {
              this.clearNumberBuffer();
              this.result.push(new Token(TokenChar.OPERATOR, "*"));
            }
            this.letterBuffer.push(ch);
          } else if (new RegExp("[-+*/%=<><=>=]").test(ch)) {

            this.clearNumberBuffer();
            this.clearLetterBuffer();
            let ch1: string = str[i+1] as string;
            if(ch1 == "="){
              this.result.push(new Token(TokenChar.OPERATOR, ch+ch1));
              i = i+1;
            }else{
              this.result.push(new Token(TokenChar.OPERATOR, ch));
            }

          } else if (new RegExp("[(]").test(ch)) {
            if (this.letterBuffer.length > 0) {
              this.result.push(new Token(TokenChar.FUNCTION,
                this.letterBuffer.join("")));
              this.letterBuffer = []
            } else if (this.numberBuffer.length > 0) {
              this.clearNumberBuffer();
              this.result.push(new Token(TokenChar.OPERATOR, "*"));
            }
            this.result.push(new Token(TokenChar.LEFT_PARENTHESIS, ch));
          } else if (new RegExp("[)]").test(ch)) {
            this.clearLetterBuffer();
            this.clearNumberBuffer();
            this.result.push(new Token(TokenChar.RIGHT_PARENTHESIS, ch));
          } else if (new RegExp("[,]").test(ch)) {
            this.clearNumberBuffer();
            this.clearLetterBuffer();
            this.result.push(new Token(TokenChar.FUNCTION_ARGS_SEPARATOR, ch));
          }
        };

        if (this.numberBuffer.length > 0) {
          this.clearNumberBuffer();
        }
        if (this.letterBuffer.length > 0) {
          this.clearLetterBuffer();
        }

        return this.result;
    }

    clearLetterBuffer() {
        let l = this.letterBuffer.length;
        for (let i = 0; i < l; i++) {
          this.result.push(new Token(TokenChar.VARIABLE, this.letterBuffer[i]));
          if (i < l - 1) {
            this.result.push(new Token(TokenChar.OPERATOR, "*"));
          }
        }
        this.letterBuffer = []
      }

      clearNumberBuffer() {
        if (this.numberBuffer.length > 0) {
          this.result.push(
            new Token(TokenChar.LITERAL, this.numberBuffer.join("")));
          this.numberBuffer = []
        }
      }

      pushIntoNumberBuffer(numbers: String[]) {
        if (numbers.length > 0) {
          numbers.forEach(num => {
            this.numberBuffer.push(num);
          });
        }
      }

      InfixToRpnTransformer(tokens: Token[]): Token[] {

        tokens.forEach(v => {
          // if the token is a number, then: push it to the output queue.
          if (v.type == TokenChar.LITERAL) {
                this.outputQueue.push(v);
                if(this.wereValuesStack.length != 0 ) {
                  this.wereValuesStack.pop();
                  this.wereValuesStack.push(new Token(TokenChar.TRUE, "true"));
                }
              }
              else if (v.type == TokenChar.FUNCTION) {
                    this.opStack.push(v);
                    this.argStack.push(0);
                    let temwereValuesStack:Token[] = []
                    if(this.wereValuesStack.length !=0) {
                      this.wereValuesStack.pop();
                      temwereValuesStack.push(new Token(TokenChar.TRUE, "true"));
                    }
                    this.wereValuesStack = (temwereValuesStack)
                    this.wereValuesStack.push(new Token(TokenChar.FALSE, "false"));
              }
              else if(v.type == TokenChar.LEFT_PARENTHESIS){
                 this.opStack.push(v);
              } else if (v.type == TokenChar.RIGHT_PARENTHESIS) {

                    while (this.opStack.length != 0  && this.opStack[this.opStack.length - 1].type==TokenChar.OPERATOR  && !(this.opStack[this.opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS)) {
                      this.outputQueue.push(this.opStack.pop());
                    }
                    if(this.opStack.length == 0){
                      // console.error("Invalid expressions !");
                      throw Error("Invalid Expression") }

                    this.opStack.pop();

                    if (this.opStack.length != 0 && this.opStack[this.opStack.length - 1].type == TokenChar.FUNCTION) {
                      // outputQueue.push(opStack.pop());
                      let f : Token  = this.opStack.pop();
                      let argsCount = this.argStack.pop();
                      let  hasValues : Token= this.wereValuesStack.pop();
                      if (hasValues.type == TokenChar.TRUE) {
                        argsCount++;
                      }
                      f.arguments = argsCount;
                      this.outputQueue.push(f);
                    }
              }else if (v.type == TokenChar.OPERATOR) {
                    while (this.opStack.length != 0 && (this.opStack[this.opStack.length - 1].type == TokenChar.OPERATOR)
                      &&
                      ((v.associativity() == "left" && (v.precedence() <= this.opStack[this.opStack.length - 1].precedence()))
                        ||
                        (v.associativity() == "right" &&
                          v.precedence() < this.opStack[this.opStack.length - 1].precedence()))) {
                      this.outputQueue.push(this.opStack.pop());
                    }
                    this.opStack.push(v);
               }else if(v.type == TokenChar.FUNCTION_ARGS_SEPARATOR){
                    while(this.opStack.length != 0 && !(this.opStack[this.opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS)) {
                      this.outputQueue.push(this.opStack.pop());
                    }
                    if(this.wereValuesStack.pop().type == TokenChar.TRUE) {
                      let argsc = this.argStack.pop();
                      argsc++;
                      this.argStack.push(argsc);
                   }
                   this.wereValuesStack.push(new Token(TokenChar.FALSE, "false"));
                  }
                })
            if(this.opStack.length != 0 && this.opStack[this.opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS){
              // console.error("Invalid expressions !");
              throw Error("Invalid Expression")
            }
            this.opStack = this.opStack.reverse();
              for (let op of this.opStack) {
                this.outputQueue.push(op);
              }
              return this.outputQueue;
      }



  expressionResolver(tokens: Token[],expressionType): Token {
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
            case "clone":
            let val: Token = rstack.pop();
            rstack.push(new Token(TokenChar.RESULT, val.value));
            break
            case "sum":
              {
                let sum = NaN
                let farguments = token.arguments;
                do{
                  farguments--;
                  let val: Token = rstack.pop();
                  if (val.value != null && val.value != "null" && isNaN(sum))
                    sum = +0
                  if (val.value != null && val.value != "null") {
                    sum = +sum + +val.value
                  }
                }while(farguments >= 1)

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
                if(less1.value != null && less1.value != "null" && less1.value != "" &&  typeof less1.value == 'string'){
                  less1.value = Number(less1.value)
                }
                if(less2.value != null && less2.value != "null" && less2.value != ""  &&  typeof less2.value == 'string'){
                  less2.value = Number(less2.value)
                }
                if (less1.value != null && less1.value != "null" && less1.value != "" && less2.value != null && less2.value != "null" && less2.value != ""  &&  (less2.value <= less1.value))
                {
                  rstack.push(new Token(TokenChar.RESULT, 1));
                }else{
                  rstack.push(new Token(TokenChar.RESULT, 0));
                }
              }
              break;
              case "if":
              {

                if(token.arguments == 0 || token.arguments == 1){
                  rstack.push(new Token(TokenChar.RESULT, rstack.pop().value));
                }else if(token.arguments==2){
                  let scoreFailure = rstack.pop()
                  let scoreSucess = rstack.pop()
                  let valueOfControlAfterConditionResolved = rstack.pop()
                  if(valueOfControlAfterConditionResolved.value==null){
                    rstack.push(new Token(TokenChar.RESULT, "null"));
                  }
                  else if(valueOfControlAfterConditionResolved.value == 1){
                    rstack.push(new Token(TokenChar.RESULT, parseInt(scoreSucess.value)));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, parseInt(scoreFailure.value)));
                  }
                }


              }
              break;
              case "selected":
              {
                let valueOfControl = rstack.pop()
                let valueToMatch = rstack.pop()
                if(valueOfControl.value == valueToMatch.value){
                  rstack.push(new Token(TokenChar.RESULT, 1));
                }else{
                  rstack.push(new Token(TokenChar.RESULT, 0));
                }
              }
              break;
          }
          break;
          case TokenChar.OPERATOR:{
            switch (token.value) {
              case "+":
                    switch(expressionType){
                      // case "constraint":
                      //   {   let sum = NaN
                      //     while (!(rstack.filter(f => (f.type == TokenChar.LITERAL || f.type == TokenChar.RESULT)).length == 0)) {
                      //       let val: Token = rstack.pop();
                      //       if (val.value != null && val.value != "null" && isNaN(sum))
                      //         sum = +1
                      //       if (val.value != null && val.value != "null") {
                      //         sum = +sum * +val.value
                      //       }
                      //     }
                      //     rstack.push(new Token(TokenChar.RESULT, sum));}
                      // break;
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
                  if(value1.value!="null" && typeof value1.value == "string"){
                    value1.value = parseInt(value1.value)
                  }
                  if(value2.value!="null" && typeof value2.value == "string"){
                    value2.value = parseInt(value2.value)
                  }

                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT,0));
                  }
                  else if(value1.value < value2.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
                case ">":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if(value1.value!="null" && typeof value1.value == "string"){
                    value1.value = parseInt(value1.value)
                  }
                  if(value2.value!="null" && typeof value2.value == "string"){
                    value2.value = parseInt(value2.value)
                  }


                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                  else if(value1.value > value2.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
                case "<=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if(value1.value!="null" && typeof value1.value == "string"){
                    value1.value = parseInt(value1.value)
                  }
                  if(value2.value!="null" && typeof value2.value == "string"){
                    value2.value = parseInt(value2.value)
                  }

                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                  else if(value2.value <= value1.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
                case ">=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if(value1.value!="null" && typeof value1.value == "string"){
                    value1.value = parseInt(value1.value)
                  }
                  if(value2.value!="null" && typeof value2.value == "string"){
                    value2.value = parseInt(value2.value)
                  }

                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT,0));
                  }
                  else if(value2.value >= value1.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
                case "=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT,  "null"));
                  }
                  else if(value1.value == value2.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
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
}
