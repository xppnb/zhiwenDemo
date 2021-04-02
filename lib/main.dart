import 'package:flutter/material.dart';
import 'package:flutter_drag_scale/core/drag_scale_widget.dart';



void main(){
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: MyHome(),
      ),
    );
  }
}

class MyHome extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      height: 400.0,
      width: 400,
      child: Center(
        child: DragScaleContainer(
          doubleTapStillScale: true,
          child: Row(
            children: [
              InkWell(
                onTap: (){
                    print("123");
                },
                child: Container(
                  width: 20,
                  height: 20,
                  color: Colors.amberAccent,
                  child: Text("123"),
                ),
              ),
              InkWell(
                child: Container(
                  width: 20,
                  height: 20,
                  color: Colors.amberAccent,
                  child: Text("123"),
                ),
              ),
              InkWell(
                child: Container(
                  width: 20,
                  height: 20,
                  color: Colors.amberAccent,
                  child: Text("123"),
                ),
              ),
              InkWell(
                child: Container(
                  width: 20,
                  height: 20,
                  color: Colors.amberAccent,
                  child: Text("123"),
                ),
              ),
            ],
          )
          )
        ),
    );
  }
}

