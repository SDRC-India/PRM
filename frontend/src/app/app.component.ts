import { Component, ViewContainerRef, ViewChild } from '@angular/core';
declare var $: any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  constructor() { }

  ngAfterViewInit() {
    $(".main-content").css("min-height", $(window).height() - 95);
  }

  ngOnInit() {
    /** start of header fix on scroll down **/
    // $(window).scroll(function () {
    //   // console.log($(window).scrollTop())
    //   if ($(window).scrollTop() > 149 && $(window).width() < 2000) {
    //     $('.nav-menu-large').addClass('navbar-fixed');
    //     $(".left-list").addClass('left-side-scroll');
    //   }
    //   if ($(window).scrollTop() < 149 && $(window).width() < 2000) {
    //     $('#header').removeClass('navbar-fixed');
    //     $(".left-list").removeClass('left-side-scroll');
    //   }
    // });
    /** end of header fix on scroll down **/
  }

  ngAfterViewChecked() {
    if ($(window).width() <= 992) {
      $(".collapse").removeClass("show");
      $(".navbar-nav .nav-item").not('.dropdown').click(function () {
        $(".collapse").removeClass("show");
      })
    }
  }

}
